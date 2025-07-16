package org.approvej.scrub;

public class ScrubbingError extends RuntimeException {
  public ScrubbingError(String message, Throwable cause) {
    super(message, cause);
  }
}
