package org.approvej.approve.review;

import java.io.IOException;
import java.nio.file.Path;

public class MeldFileReviewer implements FileReviewer {

  @Override
  public void trigger(Path receivedPath, Path approvedPath) {
    try {
      new ProcessBuilder(
              "meld",
              receivedPath.toAbsolutePath().normalize().toString(),
              approvedPath.toAbsolutePath().normalize().toString())
          .start()
          .waitFor();
    } catch (IOException | InterruptedException e) {
      throw new ReviewerError("Failed to open %s".formatted(getClass().getSimpleName()), e);
    }
  }
}
