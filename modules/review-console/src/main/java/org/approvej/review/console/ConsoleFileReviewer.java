package org.approvej.review.console;

import static java.nio.file.Files.readString;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.approvej.approve.PathProvider;
import org.approvej.review.FileReviewResult;
import org.approvej.review.FileReviewer;
import org.approvej.review.FileReviewerProvider;
import org.approvej.review.ReviewResult;
import org.jspecify.annotations.NullMarked;

/**
 * A {@link FileReviewer} that prints a colored unified diff to {@code System.out}.
 *
 * <p>This is a non-blocking reviewer: it displays the differences between the received and approved
 * files but does not approve them. The test will fail, allowing you to inspect the diff in the test
 * output and then approve the received file manually or with the {@code automatic} reviewer.
 */
@NullMarked
public final class ConsoleFileReviewer implements FileReviewer, FileReviewerProvider {

  private static final Logger LOGGER = Logger.getLogger(ConsoleFileReviewer.class.getName());

  static final String ANSI_RESET = "\033[0m";
  static final String ANSI_RED_STRIKETHROUGH = "\033[31;9m";
  static final String ANSI_GREEN_BOLD = "\033[32;1m";
  static final String ANSI_CYAN = "\033[36m";

  private final Terminal terminal;

  /** Creates a new {@link ConsoleFileReviewer}. */
  public ConsoleFileReviewer() {
    this(Terminal.system());
  }

  ConsoleFileReviewer(Terminal terminal) {
    this.terminal = terminal;
  }

  /**
   * Creates a new {@link ConsoleFileReviewer} for use with {@code reviewedBy()}.
   *
   * @return a new {@link ConsoleFileReviewer}
   */
  public static ConsoleFileReviewer console() {
    return new ConsoleFileReviewer();
  }

  @Override
  public ReviewResult apply(PathProvider pathProvider) {
    try {
      List<String> unifiedDiff =
          computeDiff(pathProvider.approvedPath(), pathProvider.receivedPath());
      boolean useColor = terminal.supportsColor();
      for (String line : unifiedDiff) {
        terminal.print((useColor ? colorize(line) : line) + "\n");
      }
    } catch (IOException exception) {
      LOGGER.info("Console review failed with exception %s.".formatted(exception));
    }
    return new FileReviewResult(false);
  }

  @Override
  public String alias() {
    return "console";
  }

  @Override
  public FileReviewer create() {
    return new ConsoleFileReviewer();
  }

  static List<String> computeDiff(Path approvedPath, Path receivedPath) throws IOException {
    String approvedContent = Files.exists(approvedPath) ? readString(approvedPath) : "";
    String receivedContent = readString(receivedPath);
    List<String> approvedLines = Arrays.asList(approvedContent.split("\n", -1));
    List<String> receivedLines = Arrays.asList(receivedContent.split("\n", -1));
    var patch = DiffUtils.diff(approvedLines, receivedLines);
    return UnifiedDiffUtils.generateUnifiedDiff(
        approvedPath.toString(), receivedPath.toString(), approvedLines, patch, 3);
  }

  static String colorize(String line) {
    if (line.startsWith("@@")) {
      return ANSI_CYAN + line + ANSI_RESET;
    }
    if (line.startsWith("+")) {
      return ANSI_GREEN_BOLD + line + ANSI_RESET;
    }
    if (line.startsWith("-")) {
      return ANSI_RED_STRIKETHROUGH + line + ANSI_RESET;
    }
    return line;
  }
}
