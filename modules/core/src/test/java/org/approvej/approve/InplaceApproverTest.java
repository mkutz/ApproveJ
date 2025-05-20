package org.approvej.approve;

import static org.approvej.approve.Approvers.value;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InplaceApproverTest {

  @Test
  void apply() {
    String previouslyApproved = "Some text";
    InplaceApprover inplaceApprover = value(previouslyApproved);

    assertThat(inplaceApprover.apply(previouslyApproved).needsApproval()).isFalse();
  }

  @Test
  void apply_previously_approved_differs() {
    String previouslyApproved = "Some other text";
    InplaceApprover inplaceApprover = value(previouslyApproved);

    assertThat(inplaceApprover.apply("Some text").needsApproval()).isTrue();
  }
}
