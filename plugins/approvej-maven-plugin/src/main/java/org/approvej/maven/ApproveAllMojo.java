package org.approvej.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/** Approves all unapproved files by moving each received file to its approved counterpart. */
@Mojo(name = "approve-all", requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
public class ApproveAllMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Override
  public void execute() throws MojoExecutionException {
    MojoHelper.executeInventory(project, "--approve-all", getLog());
  }
}
