package org.approvej.image;

import java.awt.*;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** An {@link AssertionError} thrown when an approval fails. */
@NullMarked
public class ImageApprovalError extends AssertionError {

  public ImageApprovalError(@Nullable Image previouslyApproved) {
    super(previouslyApproved == null ? "Missing approval for received" : "Approval mismatch");
  }
}
