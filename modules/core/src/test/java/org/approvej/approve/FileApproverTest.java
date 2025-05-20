package org.approvej.approve;

import static java.nio.file.Files.writeString;
import static org.approvej.approve.Approvers.file;
import static org.approvej.approve.PathProviderBuilder.approvedPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileApproverTest {

  @TempDir private Path tempDir;

  @Test
  void apply() throws IOException {
    PathProvider pathProvider = approvedPath(tempDir.resolve("some_file-approved.txt"));
    FileApprover fileApprover = file(pathProvider);
    writeString(pathProvider.approvedPath(), "Some approved text", StandardOpenOption.CREATE);

    ApprovalResult result = fileApprover.apply("Some approved text");

    assertThat(result.needsApproval()).isFalse();
    assertThat(pathProvider.receivedPath()).doesNotExist();
    assertThat(pathProvider.approvedPath()).exists().content().isEqualTo("Some approved text");
  }

  @Test
  void apply_previously_accepted_differs() throws IOException {
    PathProvider pathProvider = approvedPath(tempDir.resolve("some_file-approved.txt"));
    FileApprover fileApprover = file(pathProvider);
    writeString(pathProvider.approvedPath(), "Some approved text\n", StandardOpenOption.CREATE);

    ApprovalResult result = fileApprover.apply("Some other text");

    assertThat(result.needsApproval()).isTrue();
    assertThat(pathProvider.receivedPath()).exists().content().isEqualTo("Some other text\n");
    assertThat(pathProvider.approvedPath()).exists().content().isEqualTo("Some approved text\n");
  }

  @Test
  void apply_previously_received() throws IOException {
    PathProvider pathProvider = approvedPath(tempDir.resolve("some_file-approved.txt"));
    FileApprover fileApprover = file(pathProvider);
    writeString(pathProvider.approvedPath(), "Some approved text", StandardOpenOption.CREATE);
    writeString(pathProvider.receivedPath(), "Some received text", StandardOpenOption.CREATE);

    ApprovalResult result = fileApprover.apply("Some newly received text");

    assertThat(result.needsApproval()).isTrue();
    assertThat(pathProvider.receivedPath())
        .exists()
        .content()
        .isEqualTo("Some newly received text\n");
    assertThat(pathProvider.approvedPath()).exists().content().isEqualTo("Some approved text");
  }

  @Test
  void apply_no_previously_accepted() {
    PathProvider pathProvider = approvedPath(tempDir.resolve("some_file-approved.txt"));
    FileApprover fileApprover = file(pathProvider);

    ApprovalResult result = fileApprover.apply("Some text");

    assertThat(result.needsApproval()).isTrue();
    assertThat(pathProvider.receivedPath()).exists().content().isEqualTo("Some text\n");
    assertThat(pathProvider.approvedPath()).exists().content().isEmpty();
  }

  @Test
  void apply_no_write_access() {
    FileApprover fileApprover = file(approvedPath("/does/not/exist.txt"));

    assertThatExceptionOfType(FileApproverError.class)
        .isThrownBy(() -> fileApprover.apply("Some text"))
        .withMessage("Failed to create directories /does/not");
  }
}
