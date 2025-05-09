package org.approvej.approve;

import java.nio.file.Path;
import org.jspecify.annotations.NullMarked;

/**
 * Collection of static methods to create {@link PathProviders} instances.
 *
 * @deprecated use {@link PathProviderBuilder} instead
 */
@Deprecated(since = "0.8.3", forRemoval = true)
@NullMarked
public class PathProviders {

  private PathProviders() {}

  /**
   * Creates a new {@link ApprovedPathProvider} that uses the given approved path.
   *
   * @param approvedPath the {@link Path} to the approved file
   * @return a new {@link ApprovedPathProvider}
   * @deprecated use {@link PathProviderBuilder#approvedPath(Path)} instead
   */
  @Deprecated(since = "0.8.3", forRemoval = true)
  public static ApprovedPathProvider approvedPath(Path approvedPath) {
    return new ApprovedPathProvider(approvedPath);
  }

  /**
   * Creates a new {@link ApprovedPathProvider} that uses the given approved path.
   *
   * @param approvedPath the path to the approved file
   * @return a new {@link ApprovedPathProvider}
   * @deprecated use {@link PathProviderBuilder#approvedPath(String)} instead
   */
  @Deprecated(since = "0.8.3", forRemoval = true)
  public static ApprovedPathProvider approvedPath(String approvedPath) {
    return approvedPath(Path.of(approvedPath));
  }

  /**
   * Creates a {@link NextToTestPathProvider}.
   *
   * @return a new {@link NextToTestPathProvider}
   * @deprecated use {@link PathProviderBuilder#nextToTest()} instead
   */
  @Deprecated(since = "0.8.3", forRemoval = true)
  public static NextToTestPathProvider nextToTest() {
    return new NextToTestPathProvider();
  }
}
