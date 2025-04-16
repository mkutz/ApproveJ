package org.approvej.verify;

import static org.approvej.verify.PathProviders.nextToTest;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class NextToTestPathProviderTest {

  @Test
  void paths() {
    PathProvider pathProvider = nextToTest().build();
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
  void paths_filenameExtension() {
    PathProvider pathProvider = nextToTest().filenameExtension("json").build();
    assertThat(pathProvider.approvedPath())
        .isEqualTo(
            Path.of(
                "./src/test/java/org/approvej/verify/"
                    + "NextToTestPathProviderTest"
                    + "-paths_filenameExtension"
                    + "-approved.json"));
    assertThat(pathProvider.receivedPath())
        .isEqualTo(
            Path.of(
                "./src/test/java/org/approvej/verify/"
                    + "NextToTestPathProviderTest"
                    + "-paths_filenameExtension"
                    + "-received.json"));
  }

  @Test
  void paths_inSubdirectory() {
    PathProvider pathProvider = nextToTest().inSubdirectory().filenameExtension("json").build();
    assertThat(pathProvider.approvedPath())
        .isEqualTo(
            Path.of(
                "./src/test/java/org/approvej/verify/"
                    + "NextToTestPathProviderTest/"
                    + "paths_inSubdirectory"
                    + "-approved.json"));
    assertThat(pathProvider.receivedPath())
        .isEqualTo(
            Path.of(
                "./src/test/java/org/approvej/verify/"
                    + "NextToTestPathProviderTest/"
                    + "paths_inSubdirectory"
                    + "-received.json"));
  }
}
