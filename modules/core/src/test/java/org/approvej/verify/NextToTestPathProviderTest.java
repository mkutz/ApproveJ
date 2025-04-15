package org.approvej.verify;

import static org.approvej.verify.NextToTestPathProvider.nextToTest;
import static org.approvej.verify.NextToTestPathProvider.nextToTestAs;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class NextToTestPathProviderTest {

  @Test
  void paths() {
    var pathProvider = nextToTest();
    assertThat(pathProvider.approvedPath())
        .isEqualTo(
            Path.of(
                "./src/test/java/org/approvej/verify/"
                    + "NextToTestPathProviderTest"
                    + "-paths"
                    + "-approved.txt"));
    assertThat(pathProvider.receivedPath())
        .isEqualTo(
            Path.of(
                "./src/test/java/org/approvej/verify/"
                    + "NextToTestPathProviderTest"
                    + "-paths"
                    + "-received.txt"));
  }

  @Test
  void paths_more_complex_method_name() {
    var pathProvider = nextToTestAs("json");
    assertThat(pathProvider.approvedPath())
        .isEqualTo(
            Path.of(
                "./src/test/java/org/approvej/verify/"
                    + "NextToTestPathProviderTest"
                    + "-paths_more_complex_method_name"
                    + "-approved.json"));
    assertThat(pathProvider.receivedPath())
        .isEqualTo(
            Path.of(
                "./src/test/java/org/approvej/verify/"
                    + "NextToTestPathProviderTest"
                    + "-paths_more_complex_method_name"
                    + "-received.json"));
  }
}
