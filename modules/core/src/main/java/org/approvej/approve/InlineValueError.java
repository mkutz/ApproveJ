package org.approvej.approve;

import org.jspecify.annotations.NullMarked;

/** A {@link RuntimeException} thrown when rewriting an inline value in a test source file fails. */
@NullMarked
public class InlineValueError extends RuntimeException {

  /**
   * Creates an {@link InlineValueError} with the given message.
   *
   * @param message the error message
   */
  public InlineValueError(String message) {
    super(message);
  }

  /**
   * Creates an {@link InlineValueError} with the given message and cause.
   *
   * @param message the error message
   * @param cause the underlying cause
   */
  public InlineValueError(String message, Throwable cause) {
    super(message, cause);
  }
}
