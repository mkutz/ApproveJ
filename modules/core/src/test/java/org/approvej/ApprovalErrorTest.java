package org.approvej;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
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

  @Test
  void message_previouslyApproved_empty() {
    ApprovalError approvalError = new ApprovalError("", "something\n    actually\nreceived");

    assertThat(approvalError.getMessage())
        .isEqualTo(
            """
            Missing approval for received
              "something
                  actually
              received"
            """);
  }

  @Test
  void message_with_file_paths() {
    Path approvedPath = Path.of("/test/SomeTest-testMethod-approved.txt");
    Path receivedPath = Path.of("/test/SomeTest-testMethod-received.txt");

    ApprovalError approvalError =
        new ApprovalError("previously approved", "actually received", approvedPath, receivedPath);

    assertThat(approvalError.getMessage())
        .isEqualTo(
            """
            Approval mismatch:
            expected:
              "previously approved"
             but was:
              "actually received"
            approved: /test/SomeTest-testMethod-approved.txt
            received: /test/SomeTest-testMethod-received.txt
            """);
    assertThat(approvalError.approvedPath()).isEqualTo(approvedPath);
    assertThat(approvalError.receivedPath()).isEqualTo(receivedPath);
  }

  @Test
  void message_previouslyApproved_empty_with_file_paths() {
    Path approvedPath = Path.of("/test/SomeTest-testMethod-approved.txt");
    Path receivedPath = Path.of("/test/SomeTest-testMethod-received.txt");

    ApprovalError approvalError =
        new ApprovalError("", "actually received", approvedPath, receivedPath);

    assertThat(approvalError.getMessage())
        .isEqualTo(
            """
            Missing approval for received
              "actually received"
            approved: /test/SomeTest-testMethod-approved.txt
            received: /test/SomeTest-testMethod-received.txt
            """);
  }

  @Test
  void message_without_file_paths() {
    ApprovalError approvalError =
        new ApprovalError("previously approved", "actually received", null, null);

    assertThat(approvalError.approvedPath()).isNull();
    assertThat(approvalError.receivedPath()).isNull();
    assertThat(approvalError.getMessage()).doesNotContain("approved:");
    assertThat(approvalError.getMessage()).doesNotContain("received:");
  }
}
