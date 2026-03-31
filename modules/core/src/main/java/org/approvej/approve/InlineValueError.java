package org.approvej.approve;

import org.jspecify.annotations.NullMarked;

/** A {@link RuntimeException} thrown when rewriting an inline value in a test source file fails. */
@NullMarked
public class InlineValueError extends RuntimeException {

  public InlineValueError(String message) {
    super(message);
  }

  public InlineValueError(String message, Throwable cause) {
    super(message, cause);
  }
}
