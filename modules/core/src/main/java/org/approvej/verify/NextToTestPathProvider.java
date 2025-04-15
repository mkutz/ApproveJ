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

  /** Default filename extension for the approved and received files. */
  public static final String DEFAULT_FILENAME_EXTENSION = "txt";

  private final Path receivedPath;
  private final Path approvedPath;

  /**
   * Creates a {@link NextToTestPathProvider} with the default filename extension of {@value
   * #DEFAULT_FILENAME_EXTENSION}.
   *
   * @return a new {@link NextToTestPathProvider}
   */
  public static NextToTestPathProvider nextToTest() {
    return new NextToTestPathProvider(DEFAULT_FILENAME_EXTENSION);
  }

  /**
   * Creates a {@link NextToTestPathProvider} with the given filename extension.
   *
   * @param filenameExtension the filename extension for the approved and received files
   * @return a new {@link NextToTestPathProvider}
   */
  public static NextToTestPathProvider nextToTestAs(String filenameExtension) {
    return new NextToTestPathProvider(filenameExtension);
  }

  private NextToTestPathProvider(String filenameExtension) {
    Method method = currentTestMethod();

    String packagePath = method.getDeclaringClass().getPackageName().replace(".", "/");
    String pathRegex =
        ".*%s/%s\\.(java|kt|groovy)$"
            .formatted(packagePath, method.getDeclaringClass().getSimpleName());
    try (Stream<Path> pathStream =
        Files.find(
            Path.of("."),
            10,
            (path, attributes) ->
                attributes.isRegularFile() && path.toString().matches(pathRegex))) {
      Path testSourcePath =
          pathStream
              .findFirst()
              .orElseThrow(() -> new FileVerifierError("Could not locate test source file"));
      String fileNamePattern =
          "%s-%s-%%s.%s"
              .formatted(
                  method.getDeclaringClass().getSimpleName(), method.getName(), filenameExtension);
      this.receivedPath =
          testSourcePath.getParent().resolve(fileNamePattern.formatted(PathProvider.RECEIVED));
      this.approvedPath =
          testSourcePath.getParent().resolve(fileNamePattern.formatted(PathProvider.APPROVED));
    } catch (IOException e) {
      throw new FileVerifierError(e);
    }
  }

  @Override
  public Path approvedPath() {
    return approvedPath;
  }

  @Override
  public Path receivedPath() {
    return receivedPath;
  }
}
