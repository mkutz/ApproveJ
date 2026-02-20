package org.approvej.approve;

import static org.approvej.print.PrintFormat.DEFAULT_FILENAME_EXTENSION;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PathProviderTest {

  private final PathProvider pathProvider =
      new PathProvider(Path.of("./src/test/resources/"), "base", "affix", "approved", "xml");

  @Test
  void constructor_blank_approvedLabel() {
    assertThat(new PathProvider(Path.of("./src/test/resources/"), "base", "affix", "", "xml"))
        .hasFieldOrPropertyWithValue(
            "approvedPath",
            Path.of("./src/test/resources/" + "base" + "-affix" + ".xml").normalize())
        .hasFieldOrPropertyWithValue(
            "receivedPath",
            Path.of("./src/test/resources/" + "base" + "-affix" + "-received" + ".xml")
                .normalize());
  }

  @Test
  void directory() {
    assertThat(pathProvider.directory(Path.of("/tmp/")))
        .hasFieldOrPropertyWithValue(
            "approvedPath", Path.of("/tmp/" + "base" + "-affix" + "-approved" + ".xml").normalize())
        .hasFieldOrPropertyWithValue(
            "receivedPath",
            Path.of("/tmp/" + "base" + "-affix" + "-received" + ".xml").normalize());
  }

  @Test
  void filenameAffix() {
    assertThat(pathProvider.filenameAffix("special"))
        .hasFieldOrPropertyWithValue(
            "approvedPath",
            Path.of("./src/test/resources/" + "base" + "-special" + "-approved" + ".xml")
                .normalize())
        .hasFieldOrPropertyWithValue(
            "receivedPath",
            Path.of("./src/test/resources/" + "base" + "-special" + "-received" + ".xml")
                .normalize());
  }

  @Test
  void filenameAffix_blank() {
    assertThat(pathProvider.filenameAffix(" "))
        .hasFieldOrPropertyWithValue(
            "approvedPath",
            Path.of("./src/test/resources/" + "base" + "-approved" + ".xml").normalize())
        .hasFieldOrPropertyWithValue(
            "receivedPath",
            Path.of("./src/test/resources/" + "base" + "-received" + ".xml").normalize());
  }

  @Test
  void filenameExtension() {
    assertThat(pathProvider.filenameExtension("yml"))
        .hasFieldOrPropertyWithValue(
            "approvedPath",
            Path.of("./src/test/resources/" + "base" + "-affix" + "-approved" + ".yml").normalize())
        .hasFieldOrPropertyWithValue(
            "receivedPath",
            Path.of("./src/test/resources/" + "base" + "-affix" + "-received" + ".yml")
                .normalize());
  }

  @Test
  void filenameExtension_blank() {
    assertThat(pathProvider.filenameExtension(" "))
        .hasFieldOrPropertyWithValue(
            "approvedPath",
            Path.of("./src/test/resources/" + "base" + "-affix" + "-approved").normalize())
        .hasFieldOrPropertyWithValue(
            "receivedPath",
            Path.of("./src/test/resources/" + "base" + "-affix" + "-received").normalize());
  }

  @Test
  void filenameExtension_default_blank() {
    PathProvider pathProviderFilenameExtensionBlank = pathProvider.filenameExtension(" ");
    assertThat(pathProviderFilenameExtensionBlank.filenameExtension(DEFAULT_FILENAME_EXTENSION))
        .hasFieldOrPropertyWithValue(
            "approvedPath",
            Path.of(
                    "./src/test/resources/"
                        + "base"
                        + "-affix"
                        + "-approved"
                        + "."
                        + DEFAULT_FILENAME_EXTENSION)
                .normalize())
        .hasFieldOrPropertyWithValue(
            "receivedPath",
            Path.of(
                    "./src/test/resources/"
                        + "base"
                        + "-affix"
                        + "-received"
                        + "."
                        + DEFAULT_FILENAME_EXTENSION)
                .normalize());
  }

  @Test
  void filenameExtension_default_already_set() {
    PathProvider pathProviderFilenameExtensionAlreadySet = pathProvider.filenameExtension("xml");
    assertThat(
            pathProviderFilenameExtensionAlreadySet.filenameExtension(DEFAULT_FILENAME_EXTENSION))
        .hasFieldOrPropertyWithValue(
            "approvedPath",
            Path.of("./src/test/resources/" + "base" + "-affix" + "-approved" + ".xml").normalize())
        .hasFieldOrPropertyWithValue(
            "receivedPath",
            Path.of("./src/test/resources/" + "base" + "-affix" + "-received" + ".xml")
                .normalize());
  }

  @Test
  void approvedPath() {
    assertThat(pathProvider.approvedPath())
        .isEqualTo(
            Path.of("./src/test/resources/" + "base" + "-affix" + "-approved" + ".xml")
                .normalize());
  }

  @Test
  void receivedPath() {
    assertThat(pathProvider.receivedPath())
        .isEqualTo(
            Path.of("./src/test/resources/" + "base" + "-affix" + "-received" + ".xml")
                .normalize());
  }
}
