package org.approvej.review;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.move;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.approvej.approve.PathProvider;
import org.jspecify.annotations.NullMarked;

/**
 * A {@link FileReviewer} that calls an AI CLI tool to review the difference between the received
 * and approved files.
 *
 * <p>For text files, a unified diff is generated and included in the prompt. For image files, the
 * AI is instructed to read the files from disk.
 *
 * <p>The command can contain <code>{@value FileReviewer#RECEIVED_PLACEHOLDER}</code> and <code>
 * {@value FileReviewer#APPROVED_PLACEHOLDER}</code> placeholders, which will be replaced with the
 * actual file paths.
 *
 * <p>If the AI responds with "YES" on the first line, the received file is automatically approved.
 * Otherwise, the test fails and a human developer needs to review.
 *
 * @param command the AI CLI command to execute (e.g., "claude -p --allowedTools Read")
 */
@NullMarked
record AiFileReviewer(String command) implements FileReviewer {

  private static final Logger LOGGER = Logger.getLogger(AiFileReviewer.class.getName());

  private static final Set<String> IMAGE_EXTENSIONS =
      Set.of("png", "jpg", "jpeg", "gif", "bmp", "webp");

  private static final String TEXT_PROMPT_TEMPLATE =
      """
      You are reviewing a change in an approval test.
      Approval tests compare a "received" value (current test output) \
      against a previously "approved" value (golden master).

      Files:
      - Approved: %s
      - Received: %s

      Unified diff (--- is the approved file, +++ is the received file):
      %s

      Is this change clearly intentional and safe to approve?
      When in doubt, answer NO — it is better to ask a human than to approve an unintended change.
      Answer with exactly YES or NO on the first line, \
      followed by a summary of all differences you found.\
      """;

  private static final String IMAGE_DIFF_PROMPT_TEMPLATE =
      """
      You are reviewing a change in an image approval test.
      Approval tests compare a "received" image (current test output) \
      against a previously "approved" image (golden master).

      A pixel-difference overlay image has been generated for you:
      - Diff image: %s

      In this diff image, matching pixels are shown as dimmed grayscale, \
      and differing pixels are highlighted in magenta.

      Read the diff image file. \
      If the magenta-highlighted areas indicate clearly intentional changes \
      (e.g., expected UI updates), answer YES. \
      If they suggest potentially unintentional changes \
      (e.g., layout breaks, missing elements, changed text or numbers), answer NO.

      When in doubt, answer NO — it is better to ask a human than to approve an unintended change.
      Answer with exactly YES or NO on the first line, \
      followed by a summary of all visual differences you found.\
      """;

  private static final String IMAGE_PROMPT_TEMPLATE =
      """
      You are reviewing a change in an image approval test.
      Approval tests compare a "received" image (current test output) \
      against a previously "approved" image (golden master).

      Read and compare these two image files:
      - Approved: %s
      - Received: %s

      You MUST read both image files before answering. \
      Look at both images and determine if the visual differences are clearly intentional \
      (e.g., expected UI changes, content updates) or potentially unintentional \
      (e.g., layout breaks, missing elements, visual regressions, changed text or numbers).

      When in doubt, answer NO — it is better to ask a human than to approve an unintended change.
      Answer with exactly YES or NO on the first line, \
      followed by a summary of all visual differences you found.\
      """;

  @Override
  public ReviewResult apply(PathProvider pathProvider) {
    try {
      Path approvedPath = pathProvider.approvedPath();
      Path receivedPath = pathProvider.receivedPath();

      String prompt = buildPrompt(pathProvider, approvedPath, receivedPath);
      String resolvedCommand = FileReviewer.resolveCommand(command, approvedPath, receivedPath);
      String response = executeAiCommand(resolvedCommand, prompt);

      LOGGER.info("AI review result:\n%s".formatted(response));

      if (isApproved(response)) {
        move(receivedPath, approvedPath, REPLACE_EXISTING);
        Files.deleteIfExists(pathProvider.diffPath());
        return new FileReviewResult(true);
      }
    } catch (IOException e) {
      LOGGER.info("Review by %s failed with exception %s".formatted(getClass().getSimpleName(), e));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.info(
          "Review by %s was interrupted with exception %s"
              .formatted(getClass().getSimpleName(), e));
    }
    return new FileReviewResult(false);
  }

  private String buildPrompt(PathProvider pathProvider, Path approvedPath, Path receivedPath)
      throws IOException {
    if (isImageFile(pathProvider.filenameExtension())) {
      Path diffPath = pathProvider.diffPath();
      if (Files.exists(diffPath)) {
        return IMAGE_DIFF_PROMPT_TEMPLATE.formatted(diffPath);
      }
      return IMAGE_PROMPT_TEMPLATE.formatted(approvedPath, receivedPath);
    }
    String diff = generateDiff(approvedPath, receivedPath);
    return TEXT_PROMPT_TEMPLATE.formatted(approvedPath, receivedPath, diff);
  }

  static String generateDiff(Path approvedPath, Path receivedPath) throws IOException {
    List<String> approvedLines = readAllLines(approvedPath, UTF_8);
    List<String> receivedLines = readAllLines(receivedPath, UTF_8);
    return unifiedDiff(
        approvedPath.toString(), receivedPath.toString(), approvedLines, receivedLines);
  }

  static String unifiedDiff(
      String approvedLabel,
      String receivedLabel,
      List<String> approvedLines,
      List<String> receivedLines) {
    int[][] lengths = longestCommonSubsequenceLengths(approvedLines, receivedLines);
    List<DiffLine> diffLines = backtrack(lengths, approvedLines, receivedLines);
    if (diffLines.stream().allMatch(line -> line.type == DiffLine.Type.CONTEXT)) {
      return "";
    }
    StringBuilder result = new StringBuilder();
    result.append("--- %s\n".formatted(approvedLabel));
    result.append("+++ %s\n".formatted(receivedLabel));
    for (DiffLine line : diffLines) {
      result.append(
          switch (line.type) {
            case CONTEXT -> " %s\n".formatted(line.content);
            case REMOVED -> "-%s\n".formatted(line.content);
            case ADDED -> "+%s\n".formatted(line.content);
          });
    }
    return result.toString();
  }

  private static int[][] longestCommonSubsequenceLengths(
      List<String> approvedLines, List<String> receivedLines) {
    int approvedSize = approvedLines.size();
    int receivedSize = receivedLines.size();
    int[][] lengths = new int[approvedSize + 1][receivedSize + 1];
    for (int i = approvedSize - 1; i >= 0; i--) {
      for (int j = receivedSize - 1; j >= 0; j--) {
        if (approvedLines.get(i).equals(receivedLines.get(j))) {
          lengths[i][j] = lengths[i + 1][j + 1] + 1;
        } else {
          lengths[i][j] = Math.max(lengths[i + 1][j], lengths[i][j + 1]);
        }
      }
    }
    return lengths;
  }

  private static List<DiffLine> backtrack(
      int[][] lengths, List<String> approvedLines, List<String> receivedLines) {
    List<DiffLine> result = new ArrayList<>();
    int i = 0;
    int j = 0;
    while (i < approvedLines.size() && j < receivedLines.size()) {
      if (approvedLines.get(i).equals(receivedLines.get(j))) {
        result.add(new DiffLine(DiffLine.Type.CONTEXT, approvedLines.get(i)));
        i++;
        j++;
      } else if (lengths[i + 1][j] >= lengths[i][j + 1]) {
        result.add(new DiffLine(DiffLine.Type.REMOVED, approvedLines.get(i)));
        i++;
      } else {
        result.add(new DiffLine(DiffLine.Type.ADDED, receivedLines.get(j)));
        j++;
      }
    }
    while (i < approvedLines.size()) {
      result.add(new DiffLine(DiffLine.Type.REMOVED, approvedLines.get(i)));
      i++;
    }
    while (j < receivedLines.size()) {
      result.add(new DiffLine(DiffLine.Type.ADDED, receivedLines.get(j)));
      j++;
    }
    return result;
  }

  private record DiffLine(Type type, String content) {
    enum Type {
      CONTEXT,
      REMOVED,
      ADDED
    }
  }

  private String executeAiCommand(String resolvedCommand, String prompt)
      throws IOException, InterruptedException {
    ProcessBuilder processBuilder = new ProcessBuilder(tokenizeCommand(resolvedCommand));
    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();
    try {
      process.getOutputStream().write(prompt.getBytes(UTF_8));
      process.getOutputStream().close();
    } catch (IOException e) {
      LOGGER.fine("Writing prompt to process stdin failed: %s".formatted(e.getMessage()));
    }
    String response = new String(process.getInputStream().readAllBytes(), UTF_8);
    process.waitFor();
    return response;
  }

  private static boolean isImageFile(String filenameExtension) {
    return IMAGE_EXTENSIONS.contains(filenameExtension.toLowerCase());
  }

  private static boolean isApproved(String response) {
    return firstLine(response).trim().toUpperCase().equals("YES");
  }

  /**
   * Tokenizes a command string into a list of arguments, respecting single and double quotes. Paths
   * substituted via placeholders may contain spaces, so naive whitespace splitting is insufficient.
   */
  static List<String> tokenizeCommand(String command) {
    List<String> tokens = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean inSingleQuote = false;
    boolean inDoubleQuote = false;
    for (int i = 0; i < command.length(); i++) {
      char character = command.charAt(i);
      if (character == '\'' && !inDoubleQuote) {
        inSingleQuote = !inSingleQuote;
      } else if (character == '"' && !inSingleQuote) {
        inDoubleQuote = !inDoubleQuote;
      } else if (Character.isWhitespace(character) && !inSingleQuote && !inDoubleQuote) {
        if (!current.isEmpty()) {
          tokens.add(current.toString());
          current.setLength(0);
        }
      } else {
        current.append(character);
      }
    }
    if (!current.isEmpty()) {
      tokens.add(current.toString());
    }
    return tokens;
  }

  private static String firstLine(String response) {
    int newlineIndex = response.indexOf('\n');
    if (newlineIndex >= 0) {
      return response.substring(0, newlineIndex);
    }
    return response;
  }
}
