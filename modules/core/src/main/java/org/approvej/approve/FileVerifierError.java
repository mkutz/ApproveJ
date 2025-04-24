package org.approvej.approve;

import org.jspecify.annotations.NullMarked;

@NullMarked
class FileVerifierError extends RuntimeException {

  public FileVerifierError(String message) {
    super(message);
  }

  public FileVerifierError(String message, Throwable cause) {
    super(message, cause);
  }

  public FileVerifierError(Throwable cause) {
    super("Failed to verify file", cause);
  }
}
