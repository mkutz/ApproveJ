package org.approvej.approve;

import static org.approvej.approve.StackTraceTestFinderUtil.currentTestMethod;
import static org.approvej.approve.StackTraceTestFinderUtil.findTestSourcePath;

import java.nio.file.Path;
import org.jspecify.annotations.NullMarked;

/**
 * A {@link PathProvider} that uses a stack trace to determine the paths of the approved and
 * received files.
 *
 * @deprecated use {@link PathProviderBuilder#nextToTest()}/{@link
 *     PathProviderBuilder#nextToTestInSubdirectory()} instead
 */
@NullMarked
@Deprecated(since = "0.8.3", forRemoval = true)
public class NextToTestPathProvider implements PathProvider {

  private final Path testSourcePath;
  private final String testClassSimpleName;
  private final String testCaseName;
  private Path directory;
  private String fileNamePattern;
  private String filenameExtension;

  /**
   * Creates a new {@link NextToTestPathProvider} that uses the {@link StackTraceTestFinderUtil} to
   * determine the paths of the approved and received files.
   */
  NextToTestPathProvider() {
    TestMethod testMethod = currentTestMethod();
    this.testSourcePath = findTestSourcePath(testMethod.method());
    this.testClassSimpleName = testMethod.testClass().getSimpleName();
    this.testCaseName = testMethod.testCaseName();
    this.directory = testSourcePath.getParent();
    this.fileNamePattern = "%s-%s-%%s.%%s".formatted(testClassSimpleName, testCaseName);
    this.filenameExtension = DEFAULT_FILENAME_EXTENSION;
  }

  /**
   * Use a subdirectory named after the test class name.
   *
   * @return this
   */
  public NextToTestPathProvider inSubdirectory() {
    this.directory = testSourcePath.getParent().resolve(testClassSimpleName);
    this.fileNamePattern = "%s-%%s.%%s".formatted(testCaseName);
    return this;
  }

  /**
   * Use the given filenameExtension for the approved and received files.
   *
   * @param filenameExtension the filename extension to use
   * @return this
   */
  public NextToTestPathProvider filenameExtension(String filenameExtension) {
    this.filenameExtension = filenameExtension;
    return this;
  }

  @Override
  public Path directory() {
    return directory;
  }

  @Override
  public Path receivedPath() {
    return directory.resolve(fileNamePattern.formatted(RECEIVED, filenameExtension));
  }

  @Override
  public Path approvedPath() {
    return directory.resolve(fileNamePattern.formatted(APPROVED, filenameExtension));
  }
}
