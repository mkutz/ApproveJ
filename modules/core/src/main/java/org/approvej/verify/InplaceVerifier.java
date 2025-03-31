package org.approvej.verify;

import org.approvej.ApprovalError;
import org.jspecify.annotations.NullMarked;

/** A verifier that checks if the received value is equal to a value given in place. */
@NullMarked
public class InplaceVerifier implements Verifier {

  private final String previouslyApproved;

  /**
   * Creates a new {@link InplaceVerifier} for the given previouslyApproved value.
   *
   * @param previouslyApproved the approved value
   */
  public InplaceVerifier(String previouslyApproved) {
    this.previouslyApproved = previouslyApproved;
  }

  @Override
  public void accept(String received) {
    if (!previouslyApproved.equals(received)) {
      throw new ApprovalError(previouslyApproved, received);
    }
  }
}
