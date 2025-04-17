package org.approvej;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApprovalErrorTest {

  @Test
  void getMessage() {
    ApprovalError approvalError =
        new ApprovalError("This is a text that's different.", "This is a text that's approved.");

    assertThat(approvalError)
        .hasMessage(
            """
            Approval mismatch: \
            previously approved: <This is a text that's approved.>, \
            received: <This is a text that's different.>""");
    assertThat(approvalError.getColoredDiffMessage())
        .isEqualTo(
            """
            Approval mismatch: \
            previously approved: <\u001B[32mThis is a text that's approved.\u001B[0m>, \
            received: <\u001B[34mThis is a text that's different.\u001B[0m>
            This is a text that's \u001B[32mapproved\u001B[0m\u001B[34mdifferent\u001B[0m.""");
  }
}
