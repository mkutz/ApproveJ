package org.approvej.approve.review;

import java.nio.file.Path;

/** {@link FileReviewer} implementation that does nothing. */
public class NoFileReviewer implements FileReviewer {

  @Override
  public void trigger(Path receivedPath, Path approvedPath) {}
}
