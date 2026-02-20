package org.approvej.approve;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PathProvidersTest {

  @Test
  void approvedPath() {
    String giveApprovedPath = "./src/test/resources/some file";
    PathProvider pathProvider = PathProviders.approvedPath(giveApprovedPath);

    assertThat(pathProvider.approvedPath()).isEqualTo(Path.of(giveApprovedPath).normalize());
    assertThat(pathProvider.receivedPath())
        .isEqualTo(Path.of("./src/test/resources/some file-received").normalize());
  }

  @Test
  void nextToTest() {
    PathProvider pathProvider = PathProviders.nextToTest();

    assertThat(pathProvider.approvedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "PathProvidersTest"
                        + "-nextToTest"
                        + "-approved.txt")
                .normalize());
    assertThat(pathProvider.receivedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "PathProvidersTest"
                        + "-nextToTest"
                        + "-received.txt")
                .normalize());
  }

  @Test
  void nextToTest_filenameExtension() {
    PathProvider pathProvider = PathProviders.nextToTest().filenameExtension("json");
    assertThat(pathProvider.approvedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "PathProvidersTest"
                        + "-nextToTest_filenameExtension"
                        + "-approved.json")
                .normalize());
    assertThat(pathProvider.receivedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "PathProvidersTest"
                        + "-nextToTest_filenameExtension"
                        + "-received.json")
                .normalize());
  }

  @Test
  void nextToTestInSubdirectory() {
    PathProvider pathProvider = PathProviders.nextToTestInSubdirectory();
    assertThat(pathProvider.approvedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "PathProvidersTest/"
                        + "nextToTestInSubdirectory"
                        + "-approved.txt")
                .normalize());
    assertThat(pathProvider.receivedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "PathProvidersTest/"
                        + "nextToTestInSubdirectory"
                        + "-received.txt")
                .normalize());
  }

  @Test
  void nextToTest_directory() {
    Path directory = Path.of("./src/test/resources");
    PathProvider pathProvider = PathProviders.nextToTest().directory(directory);
    assertThat(pathProvider.approvedPath())
        .isEqualTo(
            directory
                .resolve("PathProvidersTest" + "-nextToTest_directory" + "-approved.txt")
                .normalize());
    assertThat(pathProvider.receivedPath())
        .isEqualTo(
            directory
                .resolve("PathProvidersTest" + "-nextToTest_directory" + "-received.txt")
                .normalize());
  }

  @Test
  void nextToTest_filenameAffix() {
    PathProvider pathProvider = PathProviders.nextToTest().filenameAffix("additional info");
    assertThat(pathProvider.approvedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "PathProvidersTest"
                        + "-nextToTest_filenameAffix"
                        + "-additional info"
                        + "-approved.txt")
                .normalize());
    assertThat(pathProvider.receivedPath())
        .isEqualTo(
            Path.of(
                    "./src/test/java/org/approvej/approve/"
                        + "PathProvidersTest"
                        + "-nextToTest_filenameAffix"
                        + "-additional info"
                        + "-received.txt")
                .normalize());
  }
}
