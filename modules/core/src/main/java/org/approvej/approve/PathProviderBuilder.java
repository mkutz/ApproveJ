package org.approvej.approve;

import static org.approvej.approve.StackTraceTestFinderUtil.currentTestMethod;
import static org.approvej.approve.StackTraceTestFinderUtil.findTestSourcePath;

import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.NullMarked;

/** A builder for creating {@link PathProvider} instances. */
@NullMarked
public class PathProviderBuilder {

  /**
   * The infix of the file containing the latest received value that didn't match the previously
   * approved.
   */
  public static final String RECEIVED = "received";

  /** The infix of the file containing a previously approved value. */
  public static final String APPROVED = "approved";

  private Path directory = Path.of(".");
  private String baseFilename = "";

  /**
   * Creates a new {@link PathProvider} that uses the given approved {@link Path}.
   *
   * @param approvedPath the {@link Path} to the approved file
   * @return a new {@link PathProvider}
   */
  public static PathProvider approvedPath(Path approvedPath) {
    Pattern approvedFilenamePattern =
        Pattern.compile(
            "(?<baseFilename>.+?)(?<approved>[-_. ]" + APPROVED + ")?(?:\\.(?<extension>[^.]*))?$");
    Path directory = approvedPath.getParent();
    String approvedFilename = approvedPath.getFileName().toString();
    Matcher matcher = approvedFilenamePattern.matcher(approvedFilename);
    String baseFilename = matcher.matches() ? matcher.group("baseFilename") : approvedFilename;
    String filenameExtension =
        matcher.matches() ? Objects.requireNonNullElse(matcher.group("extension"), "txt") : "txt";
    String receivedFilename = "%s-%s.%s".formatted(baseFilename, RECEIVED, filenameExtension);
    return new PathProviderRecord(directory, approvedFilename, receivedFilename);
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
   * Creates a {@link PathProviderBuilder} that uses a stack trace to determine the paths of the
   * approved and received files.
   *
   * @return a new {@link PathProviderBuilder}
   */
  public static PathProviderBuilder nextToTest() {
    TestMethod testMethod = currentTestMethod();
    return new PathProviderBuilder()
        .directory(findTestSourcePath(testMethod.method()).getParent())
        .baseFilename(
            "%s-%s".formatted(testMethod.testClass().getSimpleName(), testMethod.testCaseName()));
  }

  /**
   * Creates a {@link PathProviderBuilder} that uses a stack trace to determine the paths of the
   * approved and received files in a subdirectory named after the currect test class simple name.
   *
   * @return a new {@link PathProviderBuilder}
   */
  public static PathProviderBuilder nextToTestInSubdirectory() {
    TestMethod testMethod = currentTestMethod();
    return new PathProviderBuilder()
        .directory(
            findTestSourcePath(testMethod.method())
                .getParent()
                .resolve(testMethod.testClass().getSimpleName()))
        .baseFilename("%s".formatted(testMethod.testCaseName()));
  }

  private PathProviderBuilder() {}

  /**
   * Set the directory where the approved and received files are stored.
   *
   * @param directory the directory where the approved and received files are stored
   * @return this
   */
  public PathProviderBuilder directory(Path directory) {
    this.directory = directory;
    return this;
  }

  /**
   * Sets the {@link #baseFilename} to the given {@link String}.
   *
   * @param baseFilename the new {@link #baseFilename}
   * @return this
   */
  public PathProviderBuilder baseFilename(String baseFilename) {
    this.baseFilename = baseFilename;
    return this;
  }

  /**
   * Extends the {@link #baseFilename} with the given {@link String}.
   *
   * @param filenameAffix affix to add to the {@link #baseFilename}
   * @return this
   */
  public PathProviderBuilder filenameAffix(String filenameAffix) {
    if (filenameAffix.isBlank()) return this;
    return baseFilename("%s-%s".formatted(baseFilename, filenameAffix));
  }

  /**
   * Creates a new {@link PathProvider} with the current values and the given filenameExtension.
   *
   * @param filenameExtension the filename extension to use
   * @return a new {@link PathProvider} with the current values and the given filenameExtension
   */
  public PathProvider filenameExtension(String filenameExtension) {
    return new PathProviderRecord(
        directory,
        "%s-%s.%s".formatted(baseFilename, APPROVED, filenameExtension),
        "%s-%s.%s".formatted(baseFilename, RECEIVED, filenameExtension));
  }
}
