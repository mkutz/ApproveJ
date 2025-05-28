package org.approvej.review;

import java.nio.file.Path;
import java.util.function.BiConsumer;

/**
 * Interface for triggering a review by the user.
 *
 * <p>This usually means that a diff/merge tool is opened, which presents the difference between the
 * received and the previously approved value to users in case they differ.
 */
public interface FileReviewer extends BiConsumer<Path, Path> {

  /**
   * Triggers the review process.
   *
   * @param receivedPath the {@link Path} to the received file
   * @param approvedPath the {@link Path} to the approved file
   */
  void accept(Path receivedPath, Path approvedPath);
}
