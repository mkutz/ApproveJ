package org.approvej.verify;

import org.jspecify.annotations.NullMarked;

@NullMarked
class FileVerifierError extends RuntimeException {
  public FileVerifierError(String message) {
    super(message);
  }

  public FileVerifierError(Throwable cause) {
    super("Failed to verify file", cause);
  }
}
