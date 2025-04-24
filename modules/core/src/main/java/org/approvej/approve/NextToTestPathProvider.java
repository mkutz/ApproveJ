package org.approvej.approve;

import static org.approvej.approve.StackTraceTestFinderUtil.currentTestMethod;
import static org.approvej.approve.StackTraceTestFinderUtil.findTestSourcePath;

import java.lang.reflect.Method;
import java.nio.file.Path;
import org.jspecify.annotations.NullMarked;

/**
 * A {@link PathProvider} that uses a stack trace to determine the paths of the approved and
 * received files.
 */
@NullMarked
public class NextToTestPathProvider implements PathProvider {

  private final Path testSourcePath;
  private final String testClassSimpleName;
  private final String testMethodName;
  private Path directory;
  private String fileNamePattern;
  private String filenameExtension;

  /**
   * Creates a new {@link NextToTestPathProvider} that uses the {@link StackTraceTestFinderUtil} to
   * determine the paths of the approved and received files.
   */
  NextToTestPathProvider() {
    Method testMethod = currentTestMethod();
    this.testSourcePath = findTestSourcePath(testMethod);
    this.testClassSimpleName = testMethod.getDeclaringClass().getSimpleName();
    this.testMethodName = testMethod.getName();
    this.directory = testSourcePath.getParent();
    this.fileNamePattern = "%s-%s-%%s.%%s".formatted(testClassSimpleName, testMethodName);
    this.filenameExtension = DEFAULT_FILENAME_EXTENSION;
  }

  /**
   * Use a subdirectory named after the test class name.
   *
   * @return this
   */
  public NextToTestPathProvider inSubdirectory() {
    this.directory = testSourcePath.getParent().resolve(testClassSimpleName);
    this.fileNamePattern = "%s-%%s.%%s".formatted(testMethodName);
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
