package org.approvej.scrub;

import org.jspecify.annotations.NullMarked;

/** Exception thrown when scrubbing fails. */
@NullMarked
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
