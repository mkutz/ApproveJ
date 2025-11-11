package org.approvej.approve;

import static java.util.Objects.requireNonNullElse;
import static org.approvej.approve.PathProvider.APPROVED;
import static org.approvej.approve.StackTraceTestFinderUtil.currentTestMethod;
import static org.approvej.approve.StackTraceTestFinderUtil.findTestSourcePath;
import static org.approvej.print.PrintFormat.DEFAULT_FILENAME_EXTENSION;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Collection of static methods to create {@link PathProvider} instances. */
public final class PathProviders {

  private PathProviders() {}

  /**
   * Creates a new {@link PathProvider} that uses the given approved {@link Path}.
   *
   * @param approvedPath the {@link Path} to the approved file
   * @return a new {@link PathProvider}
   */
  public static PathProvider approvedPath(Path approvedPath) {
    Path directory = approvedPath.getParent();
    String approvedFilename = approvedPath.getFileName().toString();
    Matcher matcher =
        Pattern.compile("(?<base>.+?)(-(?<label>" + APPROVED + "))?(?:\\.(?<extension>[^.]*))?$")
            .matcher(approvedFilename);
    if (matcher.matches()) {
      String baseFilename = matcher.group("base");
      String approvedLabel = requireNonNullElse(matcher.group("label"), "");
      String filenameExtension = requireNonNullElse(matcher.group("extension"), "");
      return new PathProvider(directory, baseFilename, "", approvedLabel, filenameExtension);
    }
    return new PathProvider(directory, approvedFilename, "", "", "");
  }

  /**
   * Creates a new {@link PathProvider} that uses the given approved path}.
   *
   * @param approvedPathString the path to the approved file
   * @return a new {@link PathProvider}
   */
  public static PathProvider approvedPath(String approvedPathString) {
    return approvedPath(Path.of(approvedPathString));
  }

  /**
   * Creates a {@link PathProvider} that uses a stack trace to determine the paths of the approved
   * and received files.
   *
   * @return a new {@link PathProvider}
   */
  public static PathProvider nextToTest() {
    TestMethod testMethod = currentTestMethod();

    Path directory = findTestSourcePath(testMethod.method()).getParent();
    String baseFilename =
        "%s-%s".formatted(testMethod.testClass().getSimpleName(), testMethod.testCaseName());

    return new PathProvider(directory, baseFilename, "", APPROVED, DEFAULT_FILENAME_EXTENSION);
  }

  /**
   * Creates a {@link PathProvider} that uses a stack trace to determine the paths of the approved
   * and received files in a subdirectory named after the currect test class simple name.
   *
   * @return a new {@link PathProvider}
   */
  public static PathProvider nextToTestInSubdirectory() {
    TestMethod testMethod = currentTestMethod();

    Path directory =
        findTestSourcePath(testMethod.method())
            .getParent()
            .resolve(testMethod.testClass().getSimpleName());
    String baseFilename = testMethod.method().getName();

    return new PathProvider(directory, baseFilename, "", APPROVED, DEFAULT_FILENAME_EXTENSION);
  }
}
