package org.approvej.maven;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class MojoHelperTest {

  @Test
  void buildCommand_find() {
    var classpathElements = List.of("/lib/a.jar", "/lib/b.jar");

    var command = MojoHelper.buildCommand(classpathElements, "--find");

    String expectedJava = Path.of(System.getProperty("java.home"), "bin", "java").toString();
    String expectedClasspath = "/lib/a.jar" + System.getProperty("path.separator") + "/lib/b.jar";
    assertThat(command)
        .containsExactly(
            expectedJava,
            "-cp",
            expectedClasspath,
            "org.approvej.approve.ApprovedFileInventory",
            "--find");
  }

  @Test
  void buildCommand_remove() {
    var classpathElements = List.of("/lib/a.jar", "/lib/b.jar");

    var command = MojoHelper.buildCommand(classpathElements, "--remove");

    String expectedJava = Path.of(System.getProperty("java.home"), "bin", "java").toString();
    String expectedClasspath = "/lib/a.jar" + System.getProperty("path.separator") + "/lib/b.jar";
    assertThat(command)
        .containsExactly(
            expectedJava,
            "-cp",
            expectedClasspath,
            "org.approvej.approve.ApprovedFileInventory",
            "--remove");
  }
}
