package org.approvej.verify;

import static org.approvej.verify.StackTraceTestFinderUtil.currentTestMethod;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;

/**
 * A {@link PathProvider} that uses a stack trace to determine the paths of the approved and
 * received files.
 */
@NullMarked
public class NextToTestPathProvider implements PathProvider {

  private final Path directory;
  private final String fileNamePattern;

  /**
   * Creates a new {@link NextToTestPathProvider} that uses the {@link StackTraceTestFinderUtil} to
   * determine the paths of the approved and received files.
   *
   * @param filenameExtension the filename extension for the approved and received files
   * @param subdirectory whether to create a subdirectory for the approved and received files
   */
  NextToTestPathProvider(String filenameExtension, boolean subdirectory) {
    Method testMethod = currentTestMethod();
    Path testSourcePath = findTestSourcePath(testMethod);

    directory =
        subdirectory
            ? testSourcePath.getParent().resolve(testMethod.getDeclaringClass().getSimpleName())
            : testSourcePath.getParent();
    fileNamePattern =
        subdirectory
            ? "%s-%%s.%s".formatted(testMethod.getName(), filenameExtension)
            : "%s-%s-%%s.%s"
                .formatted(
                    testMethod.getDeclaringClass().getSimpleName(),
                    testMethod.getName(),
                    filenameExtension);
  }

  private Path findTestSourcePath(Method testMethod) {
    String packagePath = testMethod.getDeclaringClass().getPackageName().replace(".", "/");
    String pathRegex =
        ".*%s/%s\\.(java|kt|groovy)$"
            .formatted(packagePath, testMethod.getDeclaringClass().getSimpleName());
    try (Stream<Path> pathStream =
        Files.find(
            Path.of("."),
            10,
            (path, attributes) ->
                attributes.isRegularFile() && path.toString().matches(pathRegex))) {
      return pathStream
          .findFirst()
          .orElseThrow(() -> new FileVerifierError("Could not locate test source file"));
    } catch (IOException e) {
      throw new FileVerifierError(e);
    }
  }

  @Override
  public Path directory() {
    return directory;
  }

  @Override
  public Path receivedPath() {
    return directory.resolve(fileNamePattern.formatted(RECEIVED));
  }

  @Override
  public Path approvedPath() {
    return directory.resolve(fileNamePattern.formatted(APPROVED));
  }

  /** Builder for creating a {@link NextToTestPathProvider}. */
  public static class NextToTestPathProviderBuilder implements PathProviderBuilder {

    private String filenameExtension = PathProvider.DEFAULT_FILENAME_EXTENSION;
    private boolean subdirectory = false;

    /**
     * Creates a new {@link NextToTestPathProviderBuilder}.
     *
     * <p>Use {@link PathProviders#nextToTest()} to create an instance of this builder.
     */
    NextToTestPathProviderBuilder() {}

    /**
     * Sets the filename extension for the approved and received files.
     *
     * @param filenameExtension a filename extension (e.g. "json", "txt", "xml", "yaml")
     * @return this
     */
    public NextToTestPathProviderBuilder filenameExtension(String filenameExtension) {
      this.filenameExtension = filenameExtension;
      return this;
    }

    /**
     * Sets whether to create a subdirectory for the approved and received files.
     *
     * @return this
     */
    public NextToTestPathProviderBuilder inSubdirectory() {
      this.subdirectory = true;
      return this;
    }

    @Override
    public PathProvider build() {
      return new NextToTestPathProvider(filenameExtension, subdirectory);
    }
  }
}
