package org.approvej.http;

import org.jspecify.annotations.NullMarked;

/** An error that occurs when there is an issue with starting the {@link HttpStubServer}. */
@NullMarked
public class HttpStubServerException extends RuntimeException {

  /**
   * Creates a new {@link HttpStubServerException} with the given cause.
   *
   * @param cause the cause of the error
   */
  public HttpStubServerException(Throwable cause) {
    super(cause);
  }
}
