package org.approvej.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/** Reviews all unapproved files using the configured file reviewer. */
@Mojo(
    name = "review-unapproved",
    requiresDependencyResolution = ResolutionScope.TEST,
    threadSafe = true)
public class ReviewUnapprovedMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Override
  public void execute() throws MojoExecutionException {
    MojoHelper.executeInventory(project, "--review-unapproved", getLog());
  }
}
