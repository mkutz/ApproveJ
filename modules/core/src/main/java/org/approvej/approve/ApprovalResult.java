package org.approvej.approve;

import org.approvej.ApprovalError;

public interface ApprovalResult {

  String previouslyApproved();

  String received();

  default boolean needsApproval() {
    return !received().equals(previouslyApproved());
  }

  default void throwIfNotApproved() {
    if (needsApproval()) {
      throw new ApprovalError(received(), previouslyApproved());
    }
  }
}
