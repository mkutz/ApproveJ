package org.approvej.approve.review;

/** Exception thrown when a reviewer fails to open the diff tool. */
class ReviewerError extends RuntimeException {

  /**
   * Constructs a new ReviewerError with the specified detail message.
   *
   * @param message the detail message
   * @param cause the cause of the error
   */
  public ReviewerError(String message, Throwable cause) {
    super(message, cause);
  }
}
