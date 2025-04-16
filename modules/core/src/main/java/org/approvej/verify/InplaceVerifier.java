package org.approvej.verify;

import org.approvej.ApprovalError;
import org.jspecify.annotations.NullMarked;

/** A verifier that checks if the received value is equal to a value given in place. */
@NullMarked
public class InplaceVerifier implements Verifier {

  private final String previouslyApproved;

  /**
   * Creates a {@link Verifier} using the given previouslyApproved value.
   *
   * @param previouslyApproved the approved value
   */
  InplaceVerifier(String previouslyApproved) {
    this.previouslyApproved = previouslyApproved.trim();
  }

  @Override
  public void accept(String received) {
    if (!previouslyApproved.equals(received.trim())) {
      throw new ApprovalError(received, previouslyApproved);
    }
  }
}
