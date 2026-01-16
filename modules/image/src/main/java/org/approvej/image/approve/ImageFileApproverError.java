package org.approvej.image.approve;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class ImageFileApproverError extends RuntimeException {

  public ImageFileApproverError(String message) {
    super(message);
  }

  public ImageFileApproverError(String message, Throwable cause) {
    super(message, cause);
  }

  public ImageFileApproverError(Throwable cause) {
    super("Failed to approve file", cause);
  }
}
