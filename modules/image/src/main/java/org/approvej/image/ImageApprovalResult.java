package org.approvej.image;

public interface ImageApprovalResult {

  boolean needsApproval();

  void throwIfNotApproved();
}
