package org.approvej.maven;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.nio.file.Path;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

  @Test
  void executeInventory_nonzero_exit_code(@TempDir Path tempDir) {
    var project = new MavenProject();
    project.getBuild().setOutputDirectory(tempDir.resolve("classes").toString());
    project.getBuild().setTestOutputDirectory(tempDir.resolve("test-classes").toString());
    project.setFile(tempDir.resolve("pom.xml").toFile());

    assertThatExceptionOfType(MojoExecutionException.class)
        .isThrownBy(() -> MojoHelper.executeInventory(project, "--find", new SystemStreamLog()))
        .withMessageContaining("exited with code");
  }
}
