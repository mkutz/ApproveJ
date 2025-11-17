package org.approvej.approve;

import org.jspecify.annotations.NullMarked;

@NullMarked
class FileApproverError extends RuntimeException {

  public FileApproverError(String message) {
    super(message);
  }

  public FileApproverError(String message, Throwable cause) {
    super(message, cause);
  }
}
