package org.approvej.image.approve;

import org.jspecify.annotations.NullMarked;

/** Exception thrown when file operations fail during image approval. */
@NullMarked
public class ImageFileApproverError extends RuntimeException {

  /**
   * Constructs a new ImageFileApproverError with the specified detail message.
   *
   * @param message the detail message
   */
  public ImageFileApproverError(String message) {
    super(message);
  }

  /**
   * Constructs a new ImageFileApproverError with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause
   */
  public ImageFileApproverError(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new ImageFileApproverError with the specified cause.
   *
   * @param cause the cause
   */
  public ImageFileApproverError(Throwable cause) {
    super("Failed to approve file", cause);
  }
}
