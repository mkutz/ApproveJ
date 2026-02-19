package org.approvej.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/** Detects and removes orphaned approved files whose originating test method no longer exists. */
@Mojo(name = "cleanup", requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
public class CleanupMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Override
  public void execute() throws MojoExecutionException {
    MojoHelper.executeInventory(project, "--remove", getLog());
  }
}
