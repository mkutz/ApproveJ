package org.approvej;

import org.jspecify.annotations.NullMarked;

/** A verifier that checks if the received value is equal to a value given in place. */
@NullMarked
public class InplaceVerifier implements Verifier {

  private final String previouslyApproved;

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
