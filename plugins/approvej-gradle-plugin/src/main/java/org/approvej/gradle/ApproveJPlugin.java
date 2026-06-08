package org.approvej.gradle;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.concurrent.Callable;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.testing.Test;

/** Gradle plugin that registers tasks to manage approved files. */
@SuppressWarnings("unused")
public final class ApproveJPlugin implements Plugin<Project> {

  private static final String CLI_MAIN_CLASS = "org.approvej.approve.ApprovedFileInventoryCli";

  private static final String CLI_RESOURCE = "org/approvej/approve/ApprovedFileInventoryCli.class";

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
              // Combine the classpaths of every Test task. Gradle runs a source set's tests via a
              // Test task, so this resolves the inventory CLI and every recorded test class no
              // matter which source set holds the approval tests (e.g. a custom integrationTest
              // JvmTestSuite), without relying on source set naming conventions.
              FileCollection inventoryClasspath =
                  project.files(
                      (Callable<List<FileCollection>>)
                          () ->
                              project.getTasks().withType(Test.class).stream()
                                  .map(Test::getClasspath)
                                  .toList());

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
                                task.setClasspath(inventoryClasspath);
                                task.getMainClass().set(CLI_MAIN_CLASS);
                                task.args(definition.arg());
                                task.doFirst(unused -> verifyCliOnClasspath(inventoryClasspath));
                              }));
            });
  }

  /**
   * Fails fast with an actionable message when the inventory CLI is not on the resolved classpath,
   * instead of surfacing a raw {@link ClassNotFoundException} from the forked JVM.
   */
  private static void verifyCliOnClasspath(FileCollection classpath) {
    URL[] urls = classpath.getFiles().stream().map(ApproveJPlugin::toUrl).toArray(URL[]::new);
    try (URLClassLoader loader = new URLClassLoader(urls, null)) {
      if (loader.findResource(CLI_RESOURCE) == null) {
        throw new GradleException(
            ("ApproveJ's inventory CLI (%s) was not found on the classpath of any Test task. Add"
                    + " the org.approvej:core dependency to the source set that runs your approval"
                    + " tests.")
                .formatted(CLI_MAIN_CLASS));
      }
    } catch (IOException exception) {
      throw new GradleException("Failed to inspect the ApproveJ inventory classpath", exception);
    }
  }

  private static URL toUrl(java.io.File file) {
    try {
      return file.toURI().toURL();
    } catch (MalformedURLException exception) {
      throw new GradleException("Failed to resolve classpath entry %s".formatted(file), exception);
    }
  }
}
