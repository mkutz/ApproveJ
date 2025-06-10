package org.approvej.review;

import static java.nio.file.Files.writeString;
import static org.approvej.approve.PathProviderBuilder.approvedPath;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

    ReviewResult result = reviewer.apply(pathProvider);

    assertThat(result.needsReapproval()).isTrue();
  }

  @Test
  void apply_different() throws IOException {
    FileReviewerScript reviewer = new FileReviewerScript("diff {receivedFile} {approvedFile}");
    PathProvider pathProvider = approvedPath(tempDir.resolve("apply_different-approved.txt"));
    writeString(pathProvider.approvedPath(), "Some approved text", StandardOpenOption.CREATE);
    writeString(pathProvider.receivedPath(), "Some received text", StandardOpenOption.CREATE);

    ReviewResult result = reviewer.apply(pathProvider);

    assertThat(result.needsReapproval()).isFalse();
  }

  @Test
  void apply_unknown_command() throws IOException {
    FileReviewerScript reviewer =
        new FileReviewerScript("unknown-command {receivedFile} {approvedFile}");
    PathProvider pathProvider = approvedPath(tempDir.resolve("apply_different-approved.txt"));
    writeString(pathProvider.approvedPath(), "Some approved text", StandardOpenOption.CREATE);
    writeString(pathProvider.receivedPath(), "Some approved text", StandardOpenOption.CREATE);

    ReviewResult result = reviewer.apply(pathProvider);

    assertThat(result.needsReapproval()).isFalse();
  }
}
