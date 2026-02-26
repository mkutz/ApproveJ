package org.approvej.gradle;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.api.Project;
import org.gradle.api.tasks.JavaExec;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ApproveJPluginTest {

  @TempDir Path tempProjectDir;

  @Test
  void apply() {
    Project project = ProjectBuilder.builder().build();
    project.getPluginManager().apply("java");

    project.getPluginManager().apply(ApproveJPlugin.class);

    assertThat(project.getTasks().findByName("approvejFindLeftovers")).isNotNull();
    assertThat(project.getTasks().findByName("approvejCleanup")).isNotNull();
    assertThat(project.getTasks().findByName("approvejApproveAll")).isNotNull();
    assertThat(project.getTasks().findByName("approvejReviewUnapproved")).isNotNull();
  }

  @Test
  void apply_without_java_plugin() {
    Project project = ProjectBuilder.builder().build();

    project.getPluginManager().apply(ApproveJPlugin.class);

    assertThat(project.getTasks().findByName("approvejFindLeftovers")).isNull();
    assertThat(project.getTasks().findByName("approvejCleanup")).isNull();
    assertThat(project.getTasks().findByName("approvejApproveAll")).isNull();
    assertThat(project.getTasks().findByName("approvejReviewUnapproved")).isNull();
  }

  @Test
  void apply_java_plugin_after_approvej() {
    Project project = ProjectBuilder.builder().build();
    project.getPluginManager().apply(ApproveJPlugin.class);

    project.getPluginManager().apply("java");

    assertThat(project.getTasks().findByName("approvejFindLeftovers")).isNotNull();
    assertThat(project.getTasks().findByName("approvejCleanup")).isNotNull();
    assertThat(project.getTasks().findByName("approvejApproveAll")).isNotNull();
    assertThat(project.getTasks().findByName("approvejReviewUnapproved")).isNotNull();
  }

  @ParameterizedTest
  @CsvSource({
    "approvejFindLeftovers, List leftover approved files, --find-leftovers",
    "approvejCleanup, Detect and remove leftover approved files, --cleanup",
    "approvejApproveAll, Approve all unapproved files, --approve-all",
    "approvejReviewUnapproved, Review all unapproved files, --review-unapproved"
  })
  void apply_task_configuration(String taskName, String description, String arg) {
    Project project = ProjectBuilder.builder().build();
    project.getPluginManager().apply("java");
    project.getPluginManager().apply(ApproveJPlugin.class);

    var task = (JavaExec) project.getTasks().getByName(taskName);

    assertThat(task.getGroup()).isEqualTo("verification");
    assertThat(task.getDescription()).isEqualTo(description);
    assertThat(task.getMainClass().get())
        .isEqualTo("org.approvej.approve.ApprovedFileInventoryCli");
    assertThat(task.getArgs()).containsExactly(arg);
  }

  @Test
  void apply_functional() throws IOException {
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
        .contains("approvejFindLeftovers - List leftover approved files")
        .contains("approvejCleanup - Detect and remove leftover approved files")
        .contains("approvejApproveAll - Approve all unapproved files")
        .contains("approvejReviewUnapproved - Review all unapproved files");
  }

  @Test
  void apply_functional_without_java_plugin() throws IOException {
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
        .doesNotContain("approvejFindLeftovers")
        .doesNotContain("approvejCleanup")
        .doesNotContain("approvejApproveAll")
        .doesNotContain("approvejReviewUnapproved");
  }
}
