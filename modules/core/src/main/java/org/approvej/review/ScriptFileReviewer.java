package org.approvej.review;

import java.io.IOException;
import java.util.logging.Logger;
import org.approvej.approve.PathProvider;
import org.jspecify.annotations.NullMarked;

/**
 * A {@link FileReviewer} implementation that executes the given script.
 *
 * @param script the script to be executed with placeholders <code>
 *     {@value FileReviewer#RECEIVED_PLACEHOLDER}</code> and <code>
 *     {@value FileReviewer#APPROVED_PLACEHOLDER}</code>
 */
@NullMarked
record ScriptFileReviewer(String script) implements FileReviewer {

  private static final Logger LOGGER = Logger.getLogger(ScriptFileReviewer.class.getName());

  @Override
  public ReviewResult apply(PathProvider pathProvider) {
    try {
      String command = FileReviewer.resolveCommand(script, pathProvider);

      ProcessBuilder processBuilder = new ProcessBuilder();
      if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
        processBuilder.command("cmd.exe", "/c", command); // NOSONAR
      } else {
        processBuilder.command("sh", "-c", command); // NOSONAR
      }

      return new FileReviewResult(processBuilder.inheritIO().start().waitFor() == 0);
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
