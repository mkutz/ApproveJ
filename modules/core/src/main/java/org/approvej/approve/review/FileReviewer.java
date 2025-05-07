package org.approvej.approve.review;

import java.nio.file.Path;

/**
 * Interface for triggering a review by the user.
 *
 * <p>This usually means that a diff/merge tool is opened, which presents the difference between the
 * received and the previously approved value to users in case they differ.
 */
public interface FileReviewer {

  /**
   * Triggers the review process.
   *
   * @param receivedPath the {@link Path} to the received file
   * @param approvedPath the {@link Path} to the approved file
   */
  void trigger(Path receivedPath, Path approvedPath);
}
