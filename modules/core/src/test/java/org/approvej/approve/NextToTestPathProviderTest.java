package org.approvej.approve;

import static org.approvej.approve.PathProviderBuilder.nextToTest;
import static org.approvej.approve.PathProviderBuilder.nextToTestInSubdirectory;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class NextToTestPathProviderTest {

  @Test
  void paths() {
    PathProvider pathProvider = nextToTest().filenameExtension("txt");
    assertThat(pathProvider.approvedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "NextToTestPathProviderTest"
                        + "-paths"
                        + "-approved.txt")
                .toAbsolutePath()
                .normalize());
    assertThat(pathProvider.receivedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "NextToTestPathProviderTest"
                        + "-paths"
                        + "-received.txt")
                .toAbsolutePath()
                .normalize());
  }

  @Test
  void paths_filenameExtension() {
    PathProvider pathProvider = nextToTest().filenameExtension("json");
    assertThat(pathProvider.approvedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "NextToTestPathProviderTest"
                        + "-paths_filenameExtension"
                        + "-approved.json")
                .toAbsolutePath()
                .normalize());
    assertThat(pathProvider.receivedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "NextToTestPathProviderTest"
                        + "-paths_filenameExtension"
                        + "-received.json")
                .toAbsolutePath()
                .normalize());
  }

  @Test
  void paths_inSubdirectory() {
    PathProvider pathProvider = nextToTestInSubdirectory().filenameExtension("json");
    assertThat(pathProvider.approvedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "NextToTestPathProviderTest/"
                        + "paths_inSubdirectory"
                        + "-approved.json")
                .toAbsolutePath()
                .normalize());
    assertThat(pathProvider.receivedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "NextToTestPathProviderTest/"
                        + "paths_inSubdirectory"
                        + "-received.json")
                .toAbsolutePath()
                .normalize());
  }
}
