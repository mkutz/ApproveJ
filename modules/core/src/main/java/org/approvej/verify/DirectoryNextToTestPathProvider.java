package org.approvej.verify;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;

/**
 * A {@link PathProvider} that uses a stack trace to determine a directory next to the test source
 * file. The received and approved files are put into that directory.
 */
@NullMarked
public class DirectoryNextToTestPathProvider implements PathProvider {

  /** Default filename extension for the approved and received files. */
  public static final String DEFAULT_FILENAME_EXTENSION = "txt";

  private final Path receivedPath;
  private final Path approvedPath;

  /**
   * Creates a {@link DirectoryNextToTestPathProvider} with the default filename extension of
   * {@value #DEFAULT_FILENAME_EXTENSION}.
   *
   * @return a new {@link DirectoryNextToTestPathProvider}
   */
  public static DirectoryNextToTestPathProvider directoryNextToTest() {
    return new DirectoryNextToTestPathProvider(DEFAULT_FILENAME_EXTENSION);
  }

  /**
   * Creates a {@link DirectoryNextToTestPathProvider} with the given filename extension.
   *
   * @param filenameExtension the filename extension for the approved and received files
   * @return a new {@link DirectoryNextToTestPathProvider}
   */
  public static DirectoryNextToTestPathProvider directoryNextToTestAs(String filenameExtension) {
    return new DirectoryNextToTestPathProvider(filenameExtension);
  }

  private DirectoryNextToTestPathProvider(String filenameExtension) {
    Method method = StackTraceTestFinderUtil.currentTestMethod();

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
      String fileNamePattern = "%s-%%s.%s".formatted(method.getName(), filenameExtension);
      Path directory =
          testSourcePath.getParent().resolve(method.getDeclaringClass().getSimpleName());
      if (!exists(directory)) {
        createDirectories(directory);
      }
      this.receivedPath = directory.resolve(fileNamePattern.formatted(PathProvider.RECEIVED));
      this.approvedPath = directory.resolve(fileNamePattern.formatted(PathProvider.APPROVED));
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
