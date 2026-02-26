package org.approvej.gradle;

import java.util.List;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSetContainer;

/** Gradle plugin that registers tasks to find and remove leftover approved files. */
@SuppressWarnings("unused")
public final class ApproveJPlugin implements Plugin<Project> {

  record TaskDefinition(String name, String description, String arg) {}

  private static final List<TaskDefinition> TASK_DEFINITIONS =
      List.of(
          new TaskDefinition(
              "approvejFindLeftovers", "List leftover approved files", "--find-leftovers"),
          new TaskDefinition(
              "approvejCleanup", "Detect and remove leftover approved files", "--cleanup"),
          new TaskDefinition("approvejApproveAll", "Approve all unapproved files", "--approve-all"),
          new TaskDefinition(
              "approvejReviewUnapproved", "Review all unapproved files", "--review-unapproved"));

  @Override
  public void apply(Project project) {
    project
        .getPlugins()
        .withType(
            JavaPlugin.class,
            javaPlugin -> {
              var testClasspath =
                  project
                      .getExtensions()
                      .getByType(SourceSetContainer.class)
                      .getByName("test")
                      .getRuntimeClasspath();

              TASK_DEFINITIONS.forEach(
                  definition ->
                      project
                          .getTasks()
                          .register(
                              definition.name(),
                              JavaExec.class,
                              task -> {
                                task.setGroup("verification");
                                task.setDescription(definition.description());
                                task.setClasspath(testClasspath);
                                task.getMainClass()
                                    .set("org.approvej.approve.ApprovedFileInventoryCli");
                                task.args(definition.arg());
                              }));
            });
  }
}
