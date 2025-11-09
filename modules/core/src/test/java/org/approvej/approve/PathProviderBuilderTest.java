package org.approvej.approve;

import static org.approvej.approve.PathProviderBuilder.nextToTest;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PathProviderBuilderTest {

  @Test
  void paths() {
    PathProvider pathProvider = PathProviderBuilder.nextToTest();
    assertThat(pathProvider.approvedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "PathProviderBuilderTest"
                        + "-paths"
                        + "-approved.txt")
                .toAbsolutePath()
                .normalize());
    assertThat(pathProvider.receivedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "PathProviderBuilderTest"
                        + "-paths"
                        + "-received.txt")
                .toAbsolutePath()
                .normalize());
  }

  @Test
  void paths_filenameExtension() {
    PathProvider pathProvider = PathProviderBuilder.nextToTest().filenameExtension("json");
    assertThat(pathProvider.approvedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "PathProviderBuilderTest"
                        + "-paths_filenameExtension"
                        + "-approved.json")
                .toAbsolutePath()
                .normalize());
    assertThat(pathProvider.receivedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "PathProviderBuilderTest"
                        + "-paths_filenameExtension"
                        + "-received.json")
                .toAbsolutePath()
                .normalize());
  }

  @Test
  void paths_nextToTestInSubdirectory() {
    PathProvider pathProvider = PathProviderBuilder.nextToTestInSubdirectory();
    assertThat(pathProvider.approvedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "PathProviderBuilderTest/"
                        + "paths_nextToTestInSubdirectory"
                        + "-approved.txt")
                .toAbsolutePath()
                .normalize());
    assertThat(pathProvider.receivedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "PathProviderBuilderTest/"
                        + "paths_nextToTestInSubdirectory"
                        + "-received.txt")
                .toAbsolutePath()
                .normalize());
  }

  @Test
  void paths_directory() {
    PathProvider pathProvider = PathProviderBuilder.nextToTest().directory(Path.of("/tmp"));
    assertThat(pathProvider.approvedPath())
        .isEqualTo(
            Path.of(
                    "/tmp/"
                        + "PathProviderBuilderTest"
                        + "-paths_directory"
                        + "-approved.txt")
                .toAbsolutePath()
                .normalize());
    assertThat(pathProvider.receivedPath())
        .isEqualTo(
            Path.of(
                    "/tmp/"
                        + "PathProviderBuilderTest"
                        + "-paths_directory"
                        + "-received.txt")
                .toAbsolutePath()
                .normalize());
  }
  @Test
  void paths_filenameAffix() {
    PathProvider pathProvider =
        nextToTest().filenameAffix("additional info");
    assertThat(pathProvider.approvedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "PathProviderBuilderTest"
                        + "-paths_filenameAffix"
                        + "-additional info"
                        + "-approved.txt")
                .toAbsolutePath()
                .normalize());
    assertThat(pathProvider.receivedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "PathProviderBuilderTest"
                        + "-paths_filenameAffix"
                        + "-additional info"
                        + "-received.txt")
                .toAbsolutePath()
                .normalize());
  }
}
