package org.approvej.image;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Error thrown when an image approval fails. */
@NullMarked
public class ImageApprovalError extends AssertionError {

  /**
   * Constructs a new ImageApprovalError with the given description.
   *
   * @param description the description of the mismatch
   */
  public ImageApprovalError(@Nullable String description) {
    super(description == null ? "Missing approval for received image" : description);
  }
}
