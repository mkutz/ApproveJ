package org.approvej.approve;

import org.approvej.ApprovalError;
import org.jspecify.annotations.NullMarked;

/** A verifier that checks if the received value is equal to a value given in place. */
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
  public void accept(String received) {
    if (!previouslyApproved.equals(received.trim())) {
      throw new ApprovalError(received, previouslyApproved);
    }
  }
}
