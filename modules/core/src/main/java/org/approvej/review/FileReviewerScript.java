package org.approvej.review;

import java.io.IOException;
import java.nio.file.Path;

/** A {@link FileReviewer} implementation that executes the given script. */
public record FileReviewerScript(String script) implements FileReviewer {

  private static final String RECEIVED_PLACEHOLDER = "{receivedFile}";
  private static final String APPROVED_PLACEHOLDER = "{approvedFile}";

  @Override
  public void accept(Path receivedPath, Path approvedPath) {
    try {
      String command =
          script
              .replace(RECEIVED_PLACEHOLDER, receivedPath.toAbsolutePath().normalize().toString())
              .replace(APPROVED_PLACEHOLDER, approvedPath.toAbsolutePath().normalize().toString());
      Process process = new ProcessBuilder(command.split("\\s+")).start();
      process.waitFor();
    } catch (IOException | InterruptedException e) {
      throw new ReviewerError("Failed to open %s".formatted(getClass().getSimpleName()), e);
    }
  }
}
