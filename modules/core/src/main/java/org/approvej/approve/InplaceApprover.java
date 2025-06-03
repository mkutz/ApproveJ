package org.approvej.approve;

import org.approvej.ApprovalResult;
import org.jspecify.annotations.NullMarked;

/** An {@link Approver} that checks if the received value is equal to a value given in place. */
@NullMarked
public class InplaceApprover implements Approver {

  private final String previouslyApproved;

  /**
   * Creates a {@link Approver} using the given previouslyApproved value.
   *
   * @param previouslyApproved the approved value
   */
  InplaceApprover(String previouslyApproved) {
    this.previouslyApproved = previouslyApproved.trim();
  }

  @Override
  public ApprovalResult apply(String received) {
    return new InplaceApprovalResult(received.trim(), previouslyApproved);
  }
}
