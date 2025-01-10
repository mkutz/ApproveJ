package org.approvej;

import org.jspecify.annotations.NullMarked;

/** An {@link AssertionError} thrown when an approval fails. */
@NullMarked
public class ApprovalError extends AssertionError {

  /**
   * @param received the received value
   * @param previouslyApproved the previously approved value
   */
  public ApprovalError(String received, String previouslyApproved) {
    super(
        "Approval mismatch: expected: <%s> but was: <%s>".formatted(received, previouslyApproved));
  }
}
