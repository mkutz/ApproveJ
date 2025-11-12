package org.approvej.approve;

import java.nio.file.Path;
import org.jspecify.annotations.NullMarked;

/**
 * A builder for creating {@link PathProvider} instances.
 *
 * @deprecated used methods in {@link PathProviders}
 */
@NullMarked
@Deprecated(since = "0.12", forRemoval = true)
public class PathProviderBuilder {

  private PathProviderBuilder() {}

  /**
   * Creates a new {@link PathProvider} that uses the given approved {@link Path}.
   *
   * @param approvedPath the {@link Path} to the approved file
   * @return a new {@link PathProvider}
   * @deprecated use {@link PathProviders#approvedPath(Path)}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public static PathProvider approvedPath(Path approvedPath) {
    return PathProviders.approvedPath(approvedPath);
  }

  /**
   * Creates a new {@link PathProvider} that uses the given approved path}.
   *
   * @param approvedPathString the path to the approved file
   * @return a new {@link PathProvider}
   * @deprecated use {@link PathProviders#approvedPath(String)}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public static PathProvider approvedPath(String approvedPathString) {
    return PathProviders.approvedPath(Path.of(approvedPathString));
  }

  /**
   * Creates a {@link PathProvider} that uses a stack trace to determine the paths of the approved
   * and received files.
   *
   * @return a new {@link PathProvider}
   * @deprecated use {@link PathProviders#nextToTest()}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public static PathProvider nextToTest() {
    return PathProviders.nextToTest();
  }

  /**
   * Creates a {@link PathProvider} that uses a stack trace to determine the paths of the approved
   * and received files in a subdirectory named after the currect test class simple name.
   *
   * @return a new {@link PathProvider}
   * @deprecated use {@link PathProviders#nextToTestInSubdirectory()}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public static PathProvider nextToTestInSubdirectory() {
    return PathProviders.nextToTestInSubdirectory();
  }
}
