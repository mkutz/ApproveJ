package org.approvej.review.console;

import static java.nio.file.Files.writeString;
import static org.approvej.approve.PathProviders.approvedPath;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.approvej.approve.PathProvider;
import org.approvej.review.ReviewResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConsoleFileReviewerTest {

  @TempDir private Path tempDir;

  @Test
  void apply() throws IOException {
    var terminal = new NullableTerminal();
    var reviewer = new ConsoleFileReviewer(terminal);
    PathProvider pathProvider = approvedPath(tempDir.resolve("apply-approved.txt"));
    writeString(pathProvider.approvedPath(), "old content\n", StandardOpenOption.CREATE);
    writeString(pathProvider.receivedPath(), "new content\n", StandardOpenOption.CREATE);

    ReviewResult result = reviewer.apply(pathProvider);

    assertThat(result.needsReapproval()).isFalse();
    assertThat(pathProvider.approvedPath()).content().isEqualTo("old content\n");
    assertThat(pathProvider.receivedPath()).content().isEqualTo("new content\n");
    assertThat(terminal.output()).contains("-old content");
    assertThat(terminal.output()).contains("+new content");
    assertThat(terminal.output()).doesNotContain("\033[");
  }

  @Test
  void apply_colored() throws IOException {
    var terminal = new NullableTerminal(true);
    var reviewer = new ConsoleFileReviewer(terminal);
    PathProvider pathProvider = approvedPath(tempDir.resolve("colored-approved.txt"));
    writeString(pathProvider.approvedPath(), "old content\n", StandardOpenOption.CREATE);
    writeString(pathProvider.receivedPath(), "new content\n", StandardOpenOption.CREATE);

    ReviewResult result = reviewer.apply(pathProvider);

    assertThat(result.needsReapproval()).isFalse();
    assertThat(terminal.output()).contains("\033[31;9m-old content\033[0m");
    assertThat(terminal.output()).contains("\033[32;1m+new content\033[0m");
  }

  @Test
  void apply_no_approved_file() throws IOException {
    var terminal = new NullableTerminal();
    var reviewer = new ConsoleFileReviewer(terminal);
    PathProvider pathProvider = approvedPath(tempDir.resolve("new-approved.txt"));
    writeString(pathProvider.receivedPath(), "brand new content\n", StandardOpenOption.CREATE);

    ReviewResult result = reviewer.apply(pathProvider);

    assertThat(result.needsReapproval()).isFalse();
    assertThat(terminal.output()).contains("+brand new content");
  }
}
