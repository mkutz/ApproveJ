package org.approvej.review;

import static java.nio.file.Files.writeString;
import static org.approvej.approve.PathProviderBuilder.approvedPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.approvej.ApprovalResult;
import org.approvej.approve.PathProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileReviewerScriptTest {

  @TempDir private Path tempDir;

  @Test
  void apply() throws IOException {
    FileReviewerScript reviewer = new FileReviewerScript("diff {receivedFile} {approvedFile}");
    PathProvider pathProvider = approvedPath(tempDir.resolve("apply-approved.txt"));
    writeString(pathProvider.approvedPath(), "Some approved text", StandardOpenOption.CREATE);
    writeString(pathProvider.receivedPath(), "Some approved text", StandardOpenOption.CREATE);

    ApprovalResult result = reviewer.apply(pathProvider);

    assertThat(result.needsApproval()).isFalse();
  }

  @Test
  void apply_different() throws IOException {
    FileReviewerScript reviewer = new FileReviewerScript("diff {receivedFile} {approvedFile}");
    PathProvider pathProvider = approvedPath(tempDir.resolve("apply_different-approved.txt"));
    writeString(pathProvider.approvedPath(), "Some approved text", StandardOpenOption.CREATE);
    writeString(pathProvider.receivedPath(), "Some received text", StandardOpenOption.CREATE);

    ApprovalResult result = reviewer.apply(pathProvider);

    assertThat(result.needsApproval()).isTrue();
  }

  @Test
  void apply_no_file() {
    FileReviewerScript reviewer = new FileReviewerScript("diff {receivedFile} {approvedFile}");
    PathProvider pathProvider = approvedPath(tempDir.resolve("apply_no_file-approved.txt"));

    assertThatExceptionOfType(ReviewerError.class)
        .isThrownBy(() -> reviewer.apply(pathProvider))
        .withCauseInstanceOf(NoSuchFileException.class);
  }
}
