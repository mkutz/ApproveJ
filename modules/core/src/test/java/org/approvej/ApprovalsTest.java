package org.approvej;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApprovalsTest {

  @Test
  void approve() {
    String someString = "Hello World";

    assertThat(Approvals.approve(someString)).isInstanceOf(ApprovalBuilder.class);
  }
}
