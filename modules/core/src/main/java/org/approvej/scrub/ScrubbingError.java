package org.approvej.scrub;

/** Exception thrown when scrubbing fails. */
public class ScrubbingError extends RuntimeException {

  /**
   * Constructs a new ScrubbingError with the specified detail message.
   *
   * @param message the detail message
   * @param cause the cause of the error
   */
  public ScrubbingError(String message, Throwable cause) {
    super(message, cause);
  }
}
