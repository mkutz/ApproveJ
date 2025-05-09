package org.approvej.approve;

import java.nio.file.Path;
import org.jspecify.annotations.NullMarked;

/** A provider for the paths of the approved and received files. */
@NullMarked
record PathProviderRecord(Path directory, String approvedFilename, String receivedFilename)
    implements PathProvider {

  /**
   * Resolves the #approvedFilename in the #directory.
   *
   * @return the absolute and normalized {@link Path} to the approved file.
   */
  public Path approvedPath() {
    return directory.resolve(approvedFilename).toAbsolutePath().normalize();
  }

  /**
   * Resolves the #receivedFilename in the #directory.
   *
   * @return the absolute and normalized {@link Path} to the received file.
   */
  public Path receivedPath() {
    return directory.resolve(receivedFilename).toAbsolutePath().normalize();
  }
}
