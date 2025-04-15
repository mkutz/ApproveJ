package org.approvej.verify;

import static org.approvej.verify.DirectoryNextToTestPathProvider.directoryNextToTest;
import static org.approvej.verify.DirectoryNextToTestPathProvider.directoryNextToTestAs;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DirectoryNextToTestPathProviderTest {

  @Test
  void paths() {
    var pathProvider = directoryNextToTest();
    assertThat(pathProvider.approvedPath())
        .isEqualTo(
            Path.of(
                "./src/test/java/org/approvej/verify/"
                    + "DirectoryNextToTestPathProviderTest/"
                    + "paths"
                    + "-approved.txt"));
    assertThat(pathProvider.receivedPath())
        .isEqualTo(
            Path.of(
                "./src/test/java/org/approvej/verify/"
                    + "DirectoryNextToTestPathProviderTest/"
                    + "paths"
                    + "-received.txt"));
  }

  @Test
  void paths_more_complex_method_name() {
    var pathProvider = directoryNextToTestAs("json");
    assertThat(pathProvider.approvedPath())
        .isEqualTo(
            Path.of(
                "./src/test/java/org/approvej/verify/"
                    + "DirectoryNextToTestPathProviderTest/"
                    + "paths_more_complex_method_name"
                    + "-approved.json"));
    assertThat(pathProvider.receivedPath())
        .isEqualTo(
            Path.of(
                "./src/test/java/org/approvej/verify/"
                    + "DirectoryNextToTestPathProviderTest/"
                    + "paths_more_complex_method_name"
                    + "-received.json"));
  }
}
