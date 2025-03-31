package org.approvej.verify;

import static java.nio.file.Files.writeString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.approvej.ApprovalError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileVerifierTest {

  @Test
  void accept(@TempDir Path tempDir) throws IOException {
    var pathProvider = new FileVerifier.BasePathProvider(tempDir.resolve("file.txt"));
    var fileVerifier = new FileVerifier(pathProvider);
    writeString(pathProvider.approvedPath(), "Some approved text", StandardOpenOption.CREATE);

    fileVerifier.accept("Some approved text");

    assertThat(pathProvider.receivedPath()).doesNotExist();
    assertThat(pathProvider.approvedPath()).exists().content().isEqualTo("Some approved text");
  }

  @Test
  void accept_previously_accepted_differs(@TempDir Path tempDir) throws IOException {
    var pathProvider = new FileVerifier.BasePathProvider(tempDir.resolve("file.txt"));
    var fileVerifier = new FileVerifier(pathProvider);
    writeString(pathProvider.approvedPath(), "Some approved text", StandardOpenOption.CREATE);

    assertThatExceptionOfType(ApprovalError.class)
        .isThrownBy(() -> fileVerifier.accept("Some other text"))
        .withMessage(
            "Approval mismatch: expected: <Some approved text> but was: <Some other text>");

    assertThat(pathProvider.receivedPath()).exists().content().isEqualTo("Some other text");
    assertThat(pathProvider.approvedPath()).exists().content().isEqualTo("Some approved text");
  }

  @Test
  void accept_previously_received(@TempDir Path tempDir) throws IOException {
    var pathProvider = new FileVerifier.BasePathProvider(tempDir.resolve("file.txt"));
    var fileVerifier = new FileVerifier(pathProvider);
    writeString(pathProvider.approvedPath(), "Some approved text", StandardOpenOption.CREATE);
    writeString(pathProvider.receivedPath(), "Some received text", StandardOpenOption.CREATE);

    assertThatExceptionOfType(ApprovalError.class)
        .isThrownBy(() -> fileVerifier.accept("Some newly received text"))
        .withMessage(
            "Approval mismatch: expected: <Some approved text> but was: <Some newly received"
                + " text>");

    assertThat(pathProvider.receivedPath())
        .exists()
        .content()
        .isEqualTo("Some newly received text");
    assertThat(pathProvider.approvedPath()).exists().content().isEqualTo("Some approved text");
  }

  @Test
  void accept_no_previously_accepted(@TempDir Path tempDir) {
    var pathProvider = new FileVerifier.BasePathProvider(tempDir.resolve("file.txt"));
    var fileVerifier = new FileVerifier(pathProvider);

    assertThatExceptionOfType(ApprovalError.class)
        .isThrownBy(() -> fileVerifier.accept("Some text"))
        .withMessage("Approval mismatch: expected: <> but was: <Some text>");

    assertThat(pathProvider.receivedPath()).exists().content().isEqualTo("Some text");
    assertThat(pathProvider.approvedPath()).exists().content().isEmpty();
  }
}
