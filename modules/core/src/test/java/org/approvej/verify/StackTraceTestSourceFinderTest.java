package org.approvej.verify;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class StackTraceTestSourceFinderTest {

  @Test
  void currentTestSourceFile() {
    assertThat(FileVerifier.currentTestSourceFile())
        .isPresent()
        .get()
        .isEqualTo(
            Path.of("src/test/java/org/approvej/verify/StackTraceTestSourceFinderTest.java"));
  }
}
