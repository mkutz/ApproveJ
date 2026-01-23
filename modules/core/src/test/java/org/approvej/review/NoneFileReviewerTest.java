package org.approvej.review;

import static java.nio.file.Files.writeString;
import static org.approvej.approve.PathProviders.approvedPath;
import static org.approvej.review.Reviewers.none;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.approvej.approve.PathProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class NoneFileReviewerTest {

  @TempDir private Path tempDir;

  @Test
  void apply() throws IOException {
    FileReviewer reviewer = none();
    PathProvider pathProvider = approvedPath(tempDir.resolve("apply-approved.txt"));
    writeString(pathProvider.approvedPath(), "Some approved text", StandardOpenOption.CREATE);
    writeString(pathProvider.receivedPath(), "Some approved text", StandardOpenOption.CREATE);

    ReviewResult result = reviewer.apply(pathProvider);

    assertThat(result.needsReapproval()).isFalse();
  }

  @Test
  void apply_different() throws IOException {
    FileReviewer reviewer = none();
    PathProvider pathProvider = approvedPath(tempDir.resolve("apply-approved.txt"));
    writeString(pathProvider.approvedPath(), "Some approved text", StandardOpenOption.CREATE);
    writeString(pathProvider.receivedPath(), "Some received text", StandardOpenOption.CREATE);

    ReviewResult result = reviewer.apply(pathProvider);

    assertThat(result.needsReapproval()).isFalse();
  }
}
