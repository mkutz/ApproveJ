package org.approvej.image;

/** Result of evaluating an image approval. */
public interface ImageApprovalResult {

  /**
   * Indicates whether the image needs to be approved (e.g., because it didn't match the baseline).
   *
   * @return true if the image needs approval, false otherwise
   */
  boolean needsApproval();

  /** Throws an {@link ImageApprovalError} if the image was not approved. */
  void throwIfNotApproved();
}
