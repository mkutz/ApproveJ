package org.approvej.verify;

import java.nio.file.Path;
import org.approvej.verify.NextToTestPathProvider.NextToTestPathProviderBuilder;

/**
 * Collection of static methods to create {@link PathProviders} and {@link PathProviderBuilder}
 * instances.
 */
public class PathProviders {

  private PathProviders() {}

  /**
   * Creates a new {@link ApprovedPathProvider} that uses the given approved path.
   *
   * @param approvedPath the {@link Path} to the approved file
   * @return a new {@link ApprovedPathProvider}
   */
  public static ApprovedPathProvider approvedPath(Path approvedPath) {
    return new ApprovedPathProvider(approvedPath);
  }

  /**
   * Creates a new {@link ApprovedPathProvider} that uses the given approved path.
   *
   * @param approvedPath the path to the approved file
   * @return a new {@link ApprovedPathProvider}
   */
  public static ApprovedPathProvider approvedPath(String approvedPath) {
    return approvedPath(Path.of(approvedPath));
  }

  /**
   * Creates a {@link NextToTestPathProviderBuilder}.
   *
   * @return a new {@link NextToTestPathProviderBuilder}
   */
  public static NextToTestPathProviderBuilder nextToTest() {
    return new NextToTestPathProviderBuilder();
  }
}
