package org.approvej.maven;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/** Shared helper for forking a JVM to run {@code ApprovedFileInventory}. */
final class MojoHelper {

  private static final String MAIN_CLASS = "org.approvej.approve.ApprovedFileInventory";

  private MojoHelper() {}

  static void executeInventory(MavenProject project, String command, Log log)
      throws MojoExecutionException {
    List<String> classpathElements;
    try {
      classpathElements = project.getTestClasspathElements();
    } catch (Exception e) {
      throw new MojoExecutionException("Failed to resolve test classpath", e);
    }

    List<String> cmd = buildCommand(classpathElements, command);

    try {
      ProcessBuilder processBuilder = new ProcessBuilder(cmd);
      processBuilder.directory(project.getBasedir());
      processBuilder.redirectErrorStream(false);

      Process process = processBuilder.start();

      Thread stdoutThread =
          new Thread(
              () -> {
                try (BufferedReader stdout =
                    new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                  stdout.lines().forEach(log::info);
                } catch (IOException e) {
                  log.error("Error reading stdout from ApprovedFileInventory", e);
                }
              });
      Thread stderrThread =
          new Thread(
              () -> {
                try (BufferedReader stderr =
                    new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                  stderr.lines().forEach(log::error);
                } catch (IOException e) {
                  log.error("Error reading stderr from ApprovedFileInventory", e);
                }
              });
      stdoutThread.start();
      stderrThread.start();

      int exitCode = process.waitFor();
      stdoutThread.join();
      stderrThread.join();
      if (exitCode != 0) {
        throw new MojoExecutionException(
            "ApprovedFileInventory exited with code %d".formatted(exitCode));
      }
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to execute ApprovedFileInventory", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new MojoExecutionException("Interrupted while running ApprovedFileInventory", e);
    }
  }

  static List<String> buildCommand(List<String> classpathElements, String command) {
    String javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java").toString();
    String classpath = String.join(File.pathSeparator, classpathElements);

    return List.of(javaExecutable, "-cp", classpath, MAIN_CLASS, command);
  }
}
