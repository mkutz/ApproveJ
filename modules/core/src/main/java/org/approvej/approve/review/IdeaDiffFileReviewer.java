package org.approvej.approve.review;

import java.io.IOException;
import java.nio.file.Path;

public class IdeaDiffFileReviewer implements FileReviewer {

  @Override
  public void trigger(Path receivedPath, Path approvedPath) {
    try {
      new ProcessBuilder(
              switch (OS.current()) {
                case MAC -> "idea";
                case WINDOWS -> "idea64.exe";
                case LINUX -> "idea.sh";
              },
              "diff",
              receivedPath.toAbsolutePath().normalize().toString(),
              approvedPath.toAbsolutePath().normalize().toString())
          .start();
    } catch (IOException e) {
      throw new ReviewerError("Failed to open %s".formatted(getClass().getSimpleName()), e);
    }
  }
}
