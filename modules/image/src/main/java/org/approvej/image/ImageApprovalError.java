package org.approvej.image;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** An {@link AssertionError} thrown when an image approval fails. */
@NullMarked
public class ImageApprovalError extends AssertionError {

  public ImageApprovalError(@Nullable String description) {
    super(description == null ? "Missing approval for received image" : description);
  }
}
