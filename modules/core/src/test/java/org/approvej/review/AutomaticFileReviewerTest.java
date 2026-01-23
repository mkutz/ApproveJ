package org.approvej.review;

import static java.nio.file.Files.writeString;
import static org.approvej.approve.PathProviders.approvedPath;
import static org.approvej.review.Reviewers.automatic;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.approvej.approve.PathProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AutomaticFileReviewerTest {

  @TempDir private Path tempDir;

  @Test
  void apply() throws IOException {
    FileReviewer reviewer = automatic();
    PathProvider pathProvider = approvedPath(tempDir.resolve("apply-approved.txt"));
    writeString(
        pathProvider.approvedPath(),
        "Some approved text that will be ignored\n",
        StandardOpenOption.CREATE);
    writeString(
        pathProvider.receivedPath(),
        "Some received text that will be accepted automatically\n",
        StandardOpenOption.CREATE);

    ReviewResult result = reviewer.apply(pathProvider);

    assertThat(result.needsReapproval()).isTrue();
    assertThat(pathProvider.approvedPath())
        .content()
        .isEqualTo("Some received text that will be accepted automatically\n");
    assertThat(pathProvider.receivedPath()).doesNotExist();
  }
}
