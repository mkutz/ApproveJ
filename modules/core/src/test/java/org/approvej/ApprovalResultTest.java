package org.approvej;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;

import org.approvej.approve.InplaceApprovalResult;
import org.junit.jupiter.api.Test;

class ApprovalResultTest {

  private final ApprovalResult different =
      new InplaceApprovalResult("approved text", "received text");
  private final ApprovalResult equal = new InplaceApprovalResult("approved text", "approved text");

  @Test
  void needsApproval() {
    assertThat(different.needsApproval()).isTrue();
    assertThat(equal.needsApproval()).isFalse();
  }

  @Test
  void throwIfNeedsApproval() {
    assertDoesNotThrow(equal::throwIfNotApproved);
    assertThatExceptionOfType(ApprovalError.class).isThrownBy(different::throwIfNotApproved);
  }
}
