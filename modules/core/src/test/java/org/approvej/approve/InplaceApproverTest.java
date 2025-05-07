package org.approvej.approve;

import static org.approvej.approve.Approvers.value;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

import org.approvej.ApprovalError;
import org.junit.jupiter.api.Test;

class InplaceApproverTest {

  @Test
  void accept() {
    String previouslyApproved = "Some text";
    InplaceApprover inplaceApprover = value(previouslyApproved);

    assertThatNoException().isThrownBy(() -> inplaceApprover.accept(previouslyApproved));
  }

  @Test
  void accept_previously_approved_differs() {
    String previouslyApproved = "Some other text";
    InplaceApprover inplaceApprover = value(previouslyApproved);

    assertThatExceptionOfType(ApprovalError.class)
        .isThrownBy(() -> inplaceApprover.accept("Some text"));
  }
}
