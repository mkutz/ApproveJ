package org.approvej.verify;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class StackTracePathProviderTest {

  @Test
  void paths() {
    var pathProvider = new FileVerifier.StackTracePathProvider();
    assertThat(pathProvider.approvedPath())
        .isEqualTo(
            Path.of(
                "src/test/java/org/approvej/verify/StackTracePathProviderTest_paths_approved.txt"));
  }
}
