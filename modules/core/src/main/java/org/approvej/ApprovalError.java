package org.approvej;

import org.jspecify.annotations.NullMarked;

/** An {@link AssertionError} thrown when an approval fails. */
@NullMarked
public class ApprovalError extends AssertionError {

  /**
   * Creates an {@link ApprovalError} for when the given received value does not match the
   * previouslyApproved.
   *
   * @param received the received value
   * @param previouslyApproved the previously approved value
   */
  public ApprovalError(String received, String previouslyApproved) {
    super(
        "Approval mismatch: expected: <%s> but was: <%s>".formatted(previouslyApproved, received));
  }
}
