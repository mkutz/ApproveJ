package org.approvej;

/** Interface for results of approvals and reviews. */
public interface ApprovalResult {

  /**
   * Returns the previously approved value.
   *
   * @return the previously approved value
   */
  String previouslyApproved();

  /**
   * Returns the received value.
   *
   * @return the received value
   */
  String received();

  /**
   * Determines if the result needs approval by a human reviewer.
   *
   * @return true, if an approval is needed; false if the received value equals the previously
   *     approved
   */
  default boolean needsApproval() {
    return !received().equals(previouslyApproved());
  }

  /**
   * Throws an {@link ApprovalError} if {@link #needsApproval()} returns true.
   *
   * @throws ApprovalError if {@link #needsApproval()} returns true
   */
  default void throwIfNotApproved() {
    if (needsApproval()) {
      throw new ApprovalError(received(), previouslyApproved());
    }
  }
}
