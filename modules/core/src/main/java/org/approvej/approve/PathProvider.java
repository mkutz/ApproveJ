package org.approvej.approve;

import java.nio.file.Path;

/** A provider for the paths of the approved and received files. */
public interface PathProvider {

  /**
   * The infix of the file containing the latest received value that didn't match the previously
   * approved.
   */
  String RECEIVED = "received";

  /** The infix of the file containing a previously approved value. */
  String APPROVED = "approved";

  /**
   * The path of the directory containing the approved and received files.
   *
   * @return the {@link Path} to the directory
   */
  Path directory();

  /**
   * The path of the file containing the latest received value that didn't match the previously
   * approved.
   *
   * @return the {@link Path} to the received file
   */
  Path receivedPath(String filenameExtension);

  /**
   * The path of the file containing a previously approved value.
   *
   * @return the {@link Path} to the approved file
   */
  Path approvedPath(String filenameExtension);
}
