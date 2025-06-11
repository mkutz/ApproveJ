package org.approvej.review;

import java.io.IOException;
import org.approvej.approve.PathProvider;

/**
 * A {@link FileReviewer} implementation that executes the given script.
 *
 * @param script the script to be executed with placeholders <code>{@value RECEIVED_PLACEHOLDER}
 *     </code> and <code>{@value APPROVED_PLACEHOLDER}</code>
 */
public record FileReviewerScript(String script) implements FileReviewer {

  private static final String RECEIVED_PLACEHOLDER = "{receivedFile}";
  private static final String APPROVED_PLACEHOLDER = "{approvedFile}";

  /**
   * A {@link FileReviewer} implementation that executes the given script.
   *
   * @param script the script to be executed with placeholders <code>{@value RECEIVED_PLACEHOLDER}
   *     </code> and <code>{@value APPROVED_PLACEHOLDER}</code>
   * @return the new {@link FileReviewerScript}
   */
  public static FileReviewerScript script(String script) {
    return new FileReviewerScript(script);
  }

  @Override
  public ReviewResult apply(PathProvider pathProvider) {
    try {
      String command =
          script
              .replace(RECEIVED_PLACEHOLDER, "%s".formatted(pathProvider.receivedPath()))
              .replace(APPROVED_PLACEHOLDER, "%s".formatted(pathProvider.approvedPath()));

      Process process = new ProcessBuilder().command("sh", "-c", command).inheritIO().start();
      process.waitFor();

      return new FileReviewResult(process.exitValue() == 0);
    } catch (IOException e) {
      throw new ReviewerError("Review by %s failed".formatted(getClass().getSimpleName()), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ReviewerError(
          "Review by %s was interrupted".formatted(getClass().getSimpleName()), e);
    }
  }
}
