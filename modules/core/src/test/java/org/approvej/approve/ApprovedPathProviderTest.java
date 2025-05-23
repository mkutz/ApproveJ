package org.approvej.approve;

import static org.approvej.approve.PathProviderBuilder.approvedPath;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ApprovedPathProviderTest {

  @Test
  void paths() {
    Path approvedPath = Path.of("/path/to/some_file-approved.json");

    PathProvider pathProvider = approvedPath(approvedPath);

    assertThat(pathProvider.approvedPath()).isEqualTo(approvedPath);
    assertThat(pathProvider.receivedPath()).isEqualTo(Path.of("/path/to/some_file-received.json"));
  }

  @Test
  void paths_missing_infix() {
    Path approvedPathNoInfix = Path.of("/path/to/some_file.txt");

    PathProvider pathProvider = approvedPath(approvedPathNoInfix);

    assertThat(pathProvider.approvedPath()).isEqualTo(approvedPathNoInfix);
    assertThat(pathProvider.receivedPath()).isEqualTo(Path.of("/path/to/some_file-received.txt"));
  }

  @Test
  void paths_no_extension_no_infix() {
    Path approvedPathNoExtensionNoInfix = Path.of("/path/to/some_file");

    PathProvider pathProvider = approvedPath(approvedPathNoExtensionNoInfix);

    assertThat(pathProvider.approvedPath()).isEqualTo(approvedPathNoExtensionNoInfix);
    assertThat(pathProvider.receivedPath()).isEqualTo(Path.of("/path/to/some_file-received.txt"));
  }

  @Test
  void paths_no_extension() {
    Path approvedPathNoExtension = Path.of("/path/to/some_file_approved");

    PathProvider pathProvider = approvedPath(approvedPathNoExtension);

    assertThat(pathProvider.approvedPath()).isEqualTo(approvedPathNoExtension);
    assertThat(pathProvider.receivedPath()).isEqualTo(Path.of("/path/to/some_file-received.txt"));
  }
}
