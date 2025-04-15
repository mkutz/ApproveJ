package org.approvej.verify;

class FileVerifierError extends RuntimeException {
  public FileVerifierError(String message) {
    super(message);
  }

  public FileVerifierError(Throwable cause) {
    super("Failed to verify file", cause);
  }
}
