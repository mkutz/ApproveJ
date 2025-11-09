package org.approvej.approve;

import static org.approvej.approve.StackTraceTestFinderUtil.currentTestMethod;
import static org.approvej.approve.StackTraceTestFinderUtil.findTestSourcePath;
import static org.approvej.print.Printer.DEFAULT_FILENAME_EXTENSION;

import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.NullMarked;

/** A builder for creating {@link PathProvider} instances. */
@NullMarked
public class PathProviderBuilder implements PathProvider {

  /**
   * The infix of the file containing the latest received value that didn't match the previously
   * approved.
   */
  public static final String RECEIVED = "received";

  /** The infix of the file containing a previously approved value. */
  public static final String APPROVED = "approved";

  private final Path directory;

  private final String baseFilename;

  private final String filenameAffix;

  private final String filenameExtension;

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
    return new PathProviderBuilder(
        "%s-%s".formatted(testMethod.testClass().getSimpleName(), testMethod.testCaseName()),
        findTestSourcePath(testMethod.method()).getParent(),
        "",
        DEFAULT_FILENAME_EXTENSION);
  }

  /**
   * Creates a {@link PathProviderBuilder} that uses a stack trace to determine the paths of the
   * approved and received files in a subdirectory named after the currect test class simple name.
   *
   * @return a new {@link PathProviderBuilder}
   */
  public static PathProviderBuilder nextToTestInSubdirectory() {
    TestMethod testMethod = currentTestMethod();
    return new PathProviderBuilder(
        "%s".formatted(testMethod.testCaseName()),
        findTestSourcePath(testMethod.method())
            .getParent()
            .resolve(testMethod.testClass().getSimpleName()),
        "",
        DEFAULT_FILENAME_EXTENSION);
  }

  PathProviderBuilder(
      String baseFilename, Path directory, String filenameAffix, String filenameExtension) {
    this.baseFilename = baseFilename;
    this.directory = directory;
    this.filenameAffix = filenameAffix;
    this.filenameExtension = filenameExtension;
  }

  /**
   * Set the directory where the approved and received files are stored.
   *
   * @param directory the directory where the approved and received files are stored
   * @return a copy of this with the given {@link #directory}
   */
  public PathProviderBuilder directory(Path directory) {
    return new PathProviderBuilder(baseFilename, directory, filenameAffix, filenameExtension);
  }

  @Override
  public Path directory() {
    return directory;
  }

  /**
   * Extends the {@link #baseFilename} with the given {@link String}.
   *
   * @param filenameAffix affix to add to the {@link #baseFilename}
   * @return a copy of this with the given {@link #filenameAffix}
   */
  public PathProviderBuilder filenameAffix(String filenameAffix) {
    return new PathProviderBuilder(
        baseFilename,
        directory,
        !filenameAffix.isBlank() && !filenameAffix.startsWith("-")
            ? "-" + filenameAffix
            : filenameAffix,
        filenameExtension);
  }

  /**
   * Creates a new {@link PathProvider} with the current values and the given filenameExtension.
   *
   * @param filenameExtension the filename extension to use
   * @return a copy of this with the given {@link #filenameExtension}
   */
  public PathProvider filenameExtension(String filenameExtension) {
    return new PathProviderBuilder(baseFilename, directory, filenameAffix, filenameExtension);
  }

  @Override
  public Path receivedPath() {
    return directory.resolve(
        "%s%s-%s.%s".formatted(baseFilename, filenameAffix, RECEIVED, filenameExtension));
  }

  @Override
  public Path approvedPath() {
    return directory.resolve(
        "%s%s-%s.%s".formatted(baseFilename, filenameAffix, APPROVED, filenameExtension));
  }
}
