package org.approvej;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApprovalErrorTest {

  @Test
  void message() {
    ApprovalError approvalError =
        new ApprovalError(
            "something\n    previously\napproved", "something\n    actually\nreceived");

    assertThat(approvalError.getMessage())
        .isEqualTo(
            """
            Approval mismatch:
            expected:
              "something
                  previously
              approved"
             but was:
              "something
                  actually
              received"
            """);
  }
}
