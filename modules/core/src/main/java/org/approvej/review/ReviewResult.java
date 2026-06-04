package org.approvej.review;

import org.jspecify.annotations.NullMarked;

/** Interface for results of reviews. */
@NullMarked
public interface ReviewResult {

  /**
   * Determines if the approval should be reapplied.
   *
   * <p>Usually that means that approved value was changed during the review.
   *
   * @return true, if the approval should be reapplied
   */
  boolean needsReapproval();
}
