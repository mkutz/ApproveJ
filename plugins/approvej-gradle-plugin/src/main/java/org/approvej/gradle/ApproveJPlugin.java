package org.approvej.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSetContainer;

/** Gradle plugin that registers tasks to find and remove leftover approved files. */
@SuppressWarnings("unused")
public final class ApproveJPlugin implements Plugin<Project> {

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

              project
                  .getTasks()
                  .register(
                      "approvejFindLeftovers",
                      JavaExec.class,
                      task -> {
                        task.setGroup("verification");
                        task.setDescription("List leftover approved files");
                        task.setClasspath(testClasspath);
                        task.getMainClass().set("org.approvej.approve.ApprovedFileInventory");
                        task.args("--find");
                      });

              project
                  .getTasks()
                  .register(
                      "approvejCleanup",
                      JavaExec.class,
                      task -> {
                        task.setGroup("verification");
                        task.setDescription("Detect and remove leftover approved files");
                        task.setClasspath(testClasspath);
                        task.getMainClass().set("org.approvej.approve.ApprovedFileInventory");
                        task.args("--remove");
                      });
            });
  }
}
