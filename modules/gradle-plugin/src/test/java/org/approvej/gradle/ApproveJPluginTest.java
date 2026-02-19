package org.approvej.gradle;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ApproveJPluginTest {

  @TempDir Path tempProjectDir;

  @Test
  void apply() throws IOException {
    Files.writeString(
        tempProjectDir.resolve("build.gradle"),
        """
        plugins {
          id 'java'
          id 'org.approvej'
        }
        """);

    var result =
        GradleRunner.create()
            .withProjectDir(tempProjectDir.toFile())
            .withPluginClasspath()
            .withArguments("tasks", "--group", "verification")
            .build();

    assertThat(result.getOutput())
        .contains("approvejFindOrphans - List orphaned approved files")
        .contains("approvejCleanup - Detect and remove orphaned approved files");
  }

  @Test
  void apply_without_java_plugin() throws IOException {
    Files.writeString(
        tempProjectDir.resolve("build.gradle"),
        """
        plugins {
          id 'org.approvej'
        }
        """);

    var result =
        GradleRunner.create()
            .withProjectDir(tempProjectDir.toFile())
            .withPluginClasspath()
            .withArguments("tasks", "--all")
            .build();

    assertThat(result.getOutput())
        .doesNotContain("approvejFindOrphans")
        .doesNotContain("approvejCleanup");
  }
}
