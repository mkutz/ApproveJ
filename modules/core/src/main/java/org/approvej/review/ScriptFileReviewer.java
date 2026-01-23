package org.approvej.review;

import java.io.IOException;
import java.util.logging.Logger;
import org.approvej.approve.PathProvider;
import org.jspecify.annotations.NullMarked;

/**
 * A {@link FileReviewer} implementation that executes the given script.
 *
 * @param script the script to be executed with placeholders <code>{@value RECEIVED_PLACEHOLDER}
 *     </code> and <code>{@value APPROVED_PLACEHOLDER}</code>
 */
@NullMarked
record ScriptFileReviewer(String script) implements FileReviewer {

  static final String RECEIVED_PLACEHOLDER = "{receivedFile}";
  static final String APPROVED_PLACEHOLDER = "{approvedFile}";
  private static final Logger LOGGER = Logger.getLogger(ScriptFileReviewer.class.getName());

  /**
   * A {@link FileReviewer} implementation that executes the given script.
   *
   * @param script the script to be executed with placeholders <code>{@value RECEIVED_PLACEHOLDER}
   *     </code> and <code>{@value APPROVED_PLACEHOLDER}</code>
   * @return the new {@link ScriptFileReviewer}
   * @deprecated use {@link Reviewers#script(String)} instead
   */
  @Deprecated(forRemoval = true, since = "1.1")
  public static ScriptFileReviewer script(String script) {
    return new ScriptFileReviewer(script);
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
      LOGGER.info("Review by %s failed with exception %s".formatted(getClass().getSimpleName(), e));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.info(
          "Review by %s was interrupted with exception %s"
              .formatted(getClass().getSimpleName(), e));
    }
    return new FileReviewResult(false);
  }
}
