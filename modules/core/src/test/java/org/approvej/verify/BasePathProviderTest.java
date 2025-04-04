package org.approvej.verify;

import static org.approvej.verify.FileVerifier.BasePathProvider.approvedPath;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class BasePathProviderTest {

  @Test
  void paths() {
    Path approvedPath = Path.of("/path/to/some_file-approved.json");

    var pathProvider = approvedPath(approvedPath);

    assertThat(pathProvider.approvedPath()).isEqualTo(approvedPath);
    assertThat(pathProvider.receivedPath()).isEqualTo(Path.of("/path/to/some_file-received.json"));
  }

  @Test
  void paths_missing_infix() {
    Path approvedPathNoInfix = Path.of("/path/to/some_file.txt");

    var pathProvider = approvedPath(approvedPathNoInfix);

    assertThat(pathProvider.approvedPath()).isEqualTo(approvedPathNoInfix);
    assertThat(pathProvider.receivedPath()).isEqualTo(Path.of("/path/to/some_file-received.txt"));
  }

  @Test
  void paths_no_extension() {
    Path approvedPathNoInfix = Path.of("/path/to/some_file");

    var pathProvider = approvedPath(approvedPathNoInfix);

    assertThat(pathProvider.approvedPath()).isEqualTo(approvedPathNoInfix);
    assertThat(pathProvider.receivedPath()).isEqualTo(Path.of("/path/to/some_file-received.txt"));
  }
}
