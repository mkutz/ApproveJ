package org.approvej.approve;

import org.approvej.ApprovalResult;
import org.jspecify.annotations.NullMarked;

/**
 * An {@link Approver} that checks if the received value is equal to a value given in place.
 *
 * @param previouslyApproved the approved value
 */
@NullMarked
record InplaceApprover(String previouslyApproved) implements Approver {

  /** Creates a {@link Approver} using the given previouslyApproved value. */
  public InplaceApprover(String previouslyApproved) {
    this.previouslyApproved = previouslyApproved.trim();
  }

  @Override
  public ApprovalResult apply(String received) {
    return new InplaceApprovalResult(received.trim(), previouslyApproved);
  }
}
