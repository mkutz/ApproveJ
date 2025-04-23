package org.approvej.verify;

import static java.nio.file.Files.writeString;
import static org.approvej.verify.PathProviders.approvedPath;
import static org.approvej.verify.Verifiers.file;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.approvej.ApprovalError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileVerifierTest {

  @TempDir private Path tempDir;

  @Test
  void accept() throws IOException {
    ApprovedPathProvider pathProvider = approvedPath(tempDir.resolve("some_file-approved.txt"));
    FileVerifier fileVerifier = Verifiers.file(pathProvider);
    writeString(pathProvider.approvedPath(), "Some approved text", StandardOpenOption.CREATE);

    fileVerifier.accept("Some approved text");

    assertThat(pathProvider.receivedPath()).doesNotExist();
    assertThat(pathProvider.approvedPath()).exists().content().isEqualTo("Some approved text");
  }

  @Test
  void accept_previously_accepted_differs() throws IOException {
    ApprovedPathProvider pathProvider = approvedPath(tempDir.resolve("some_file-approved.txt"));
    FileVerifier fileVerifier = Verifiers.file(pathProvider);
    writeString(pathProvider.approvedPath(), "Some approved text", StandardOpenOption.CREATE);

    assertThatExceptionOfType(ApprovalError.class)
        .isThrownBy(() -> fileVerifier.accept("Some other text"))
        .withMessage(
            "Approval mismatch: expected: <Some approved text> but was: <Some other text>");

    assertThat(pathProvider.receivedPath()).exists().content().isEqualTo("Some other text");
    assertThat(pathProvider.approvedPath()).exists().content().isEqualTo("Some approved text");
  }

  @Test
  void accept_previously_received() throws IOException {
    ApprovedPathProvider pathProvider = approvedPath(tempDir.resolve("some_file-approved.txt"));
    FileVerifier fileVerifier = Verifiers.file(pathProvider);
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
  void accept_no_previously_accepted() {
    ApprovedPathProvider pathProvider = approvedPath(tempDir.resolve("some_file-approved.txt"));
    FileVerifier fileVerifier = Verifiers.file(pathProvider);

    assertThatExceptionOfType(ApprovalError.class)
        .isThrownBy(() -> fileVerifier.accept("Some text"))
        .withMessage("Approval mismatch: expected: <> but was: <Some text>");

    assertThat(pathProvider.receivedPath()).exists().content().isEqualTo("Some text");
    assertThat(pathProvider.approvedPath()).exists().content().isEmpty();
  }

  @Test
  void accept_no_write_access() {
    FileVerifier fileVerifier = file("/does/not/exist.txt");

    assertThatExceptionOfType(FileVerifierError.class)
        .isThrownBy(() -> fileVerifier.accept("Some text"))
        .withMessage("Failed to create directories /does/not");
  }
}
