package org.approvej.approve;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.nio.file.Path;
import org.approvej.ApprovalError;
import org.junit.jupiter.api.Test;

class FileApprovalResultTest {

  private final PathProvider pathProvider =
      new PathProvider(Path.of("/test"), "SomeTest-testMethod", "", "approved", "txt");

  @Test
  void throwIfNotApproved() {
    var result = new FileApprovalResult("approved text", "received text", pathProvider);

    assertThatExceptionOfType(ApprovalError.class)
        .isThrownBy(result::throwIfNotApproved)
        .satisfies(
            error -> {
              assertThat(error.approvedPath()).isEqualTo(pathProvider.approvedPath());
              assertThat(error.receivedPath()).isEqualTo(pathProvider.receivedPath());
              assertThat(error.getMessage()).contains(pathProvider.approvedPath().toString());
              assertThat(error.getMessage()).contains(pathProvider.receivedPath().toString());
            });
  }

  @Test
  void throwIfNotApproved_equal() {
    var result = new FileApprovalResult("same text", "same text", pathProvider);

    assertDoesNotThrow(result::throwIfNotApproved);
  }
}
