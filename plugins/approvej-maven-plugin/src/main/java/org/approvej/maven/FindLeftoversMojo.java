package org.approvej.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/** Lists leftover approved files whose originating test method no longer exists. */
@Mojo(
    name = "find-leftovers",
    requiresDependencyResolution = ResolutionScope.TEST,
    threadSafe = true)
public class FindLeftoversMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Override
  public void execute() throws MojoExecutionException {
    MojoHelper.executeInventory(project, "--find", getLog());
  }
}
