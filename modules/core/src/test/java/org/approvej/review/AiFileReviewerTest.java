package org.approvej.review;

import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;
import static org.approvej.approve.PathProviders.approvedPath;
import static org.approvej.review.Reviewers.ai;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.approvej.approve.PathProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AiFileReviewerTest {

  @TempDir private Path tempDir;

  @Test
  void apply() throws IOException {
    FileReviewer reviewer = ai("cat");
    PathProvider pathProvider = approvedPath(tempDir.resolve("apply-approved.txt"));
    writeString(pathProvider.approvedPath(), "Some approved text", StandardOpenOption.CREATE);
    writeString(pathProvider.receivedPath(), "Some received text", StandardOpenOption.CREATE);

    ReviewResult result = reviewer.apply(pathProvider);

    assertThat(result.needsReapproval()).isFalse();
  }

  @Test
  void apply_approved() throws IOException {
    FileReviewer reviewer = ai("echo YES");
    PathProvider pathProvider = approvedPath(tempDir.resolve("apply_approved-approved.txt"));
    writeString(pathProvider.approvedPath(), "Some approved text", StandardOpenOption.CREATE);
    writeString(pathProvider.receivedPath(), "Some received text", StandardOpenOption.CREATE);

    ReviewResult result = reviewer.apply(pathProvider);

    assertThat(result.needsReapproval()).isTrue();
    assertThat(readString(pathProvider.approvedPath())).isEqualTo("Some received text");
  }

  @Test
  void apply_approved_cleans_up_diff_file() throws IOException {
    FileReviewer reviewer = ai("echo YES");
    PathProvider pathProvider = approvedPath(tempDir.resolve("apply_cleanup-approved.txt"));
    writeString(pathProvider.approvedPath(), "Some approved text", StandardOpenOption.CREATE);
    writeString(pathProvider.receivedPath(), "Some received text", StandardOpenOption.CREATE);
    writeString(pathProvider.diffPath(), "diff content", StandardOpenOption.CREATE);

    ReviewResult result = reviewer.apply(pathProvider);

    assertThat(result.needsReapproval()).isTrue();
    assertThat(pathProvider.diffPath()).doesNotExist();
  }

  @Test
  void apply_rejected() throws IOException {
    FileReviewer reviewer = ai("echo NO");
    PathProvider pathProvider = approvedPath(tempDir.resolve("apply_rejected-approved.txt"));
    writeString(pathProvider.approvedPath(), "Some approved text", StandardOpenOption.CREATE);
    writeString(pathProvider.receivedPath(), "Some received text", StandardOpenOption.CREATE);

    ReviewResult result = reviewer.apply(pathProvider);

    assertThat(result.needsReapproval()).isFalse();
    assertThat(readString(pathProvider.approvedPath())).isEqualTo("Some approved text");
  }

  @Test
  void apply_unknown_command() throws IOException {
    FileReviewer reviewer = ai("unknown-command-that-does-not-exist");
    PathProvider pathProvider = approvedPath(tempDir.resolve("apply_unknown_command-approved.txt"));
    writeString(pathProvider.approvedPath(), "Some approved text", StandardOpenOption.CREATE);
    writeString(pathProvider.receivedPath(), "Some received text", StandardOpenOption.CREATE);

    ReviewResult result = reviewer.apply(pathProvider);

    assertThat(result.needsReapproval()).isFalse();
  }

  @Test
  void apply_image_files() throws IOException {
    FileReviewer reviewer = ai("echo YES");
    PathProvider pathProvider = approvedPath(tempDir.resolve("apply_image_files-approved.png"));
    writeString(pathProvider.approvedPath(), "fake image data", StandardOpenOption.CREATE);
    writeString(pathProvider.receivedPath(), "fake received image data", StandardOpenOption.CREATE);

    ReviewResult result = reviewer.apply(pathProvider);

    assertThat(result.needsReapproval()).isTrue();
    assertThat(readString(pathProvider.approvedPath())).isEqualTo("fake received image data");
  }

  @Test
  void apply_image_files_with_diff_file() throws IOException {
    FileReviewer reviewer = ai("echo YES");
    PathProvider pathProvider = approvedPath(tempDir.resolve("apply_image_diff-approved.png"));
    writeString(pathProvider.approvedPath(), "fake image data", StandardOpenOption.CREATE);
    writeString(pathProvider.receivedPath(), "fake received image data", StandardOpenOption.CREATE);
    writeString(pathProvider.diffPath(), "fake diff image data", StandardOpenOption.CREATE);

    ReviewResult result = reviewer.apply(pathProvider);

    assertThat(result.needsReapproval()).isTrue();
    assertThat(pathProvider.diffPath()).doesNotExist();
  }

  @Test
  void apply_image_files_rejected() throws IOException {
    FileReviewer reviewer = ai("echo NO");
    PathProvider pathProvider = approvedPath(tempDir.resolve("apply_image_rejected-approved.png"));
    writeString(pathProvider.approvedPath(), "fake image data", StandardOpenOption.CREATE);
    writeString(pathProvider.receivedPath(), "fake received image data", StandardOpenOption.CREATE);

    ReviewResult result = reviewer.apply(pathProvider);

    assertThat(result.needsReapproval()).isFalse();
    assertThat(readString(pathProvider.approvedPath())).isEqualTo("fake image data");
  }

  @Test
  void apply_no_received_file() {
    FileReviewer reviewer = ai("echo YES");
    PathProvider pathProvider = approvedPath(tempDir.resolve("apply_no_received-approved.txt"));

    ReviewResult result = reviewer.apply(pathProvider);

    assertThat(result.needsReapproval()).isFalse();
  }

  @Test
  void apply_command_with_placeholders() throws IOException {
    PathProvider pathProvider = approvedPath(tempDir.resolve("apply_placeholders-approved.txt"));
    writeString(pathProvider.approvedPath(), "approved", StandardOpenOption.CREATE);
    writeString(pathProvider.receivedPath(), "received", StandardOpenOption.CREATE);

    Path script = tempDir.resolve("reviewer.sh");
    writeString(script, "#!/bin/sh\necho \"YES\"\n", StandardOpenOption.CREATE);
    Files.setPosixFilePermissions(
        script, java.nio.file.attribute.PosixFilePermissions.fromString("rwxr-xr-x"));

    FileReviewer reviewer = ai(script + " {receivedFile} {approvedFile}");

    ReviewResult result = reviewer.apply(pathProvider);

    assertThat(result.needsReapproval()).isTrue();
  }
}
