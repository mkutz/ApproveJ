package org.approvej.approve;

import static java.util.Arrays.stream;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;

/** Utility class to find the current test method using the stack trace. */
@NullMarked
public class StackTraceTestFinderUtil {

  private StackTraceTestFinderUtil() {
    // Util class
  }

  /**
   * Finds the current test method using the stack trace.
   *
   * @return the currently executing test {@link Method}
   */
  public static TestMethod currentTestMethod() {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    return stream(stackTrace)
        .map(
            element -> {
              try {
                return Class.forName(element.getClassName())
                    .getDeclaredMethod(
                        element.getMethodName().replaceAll("^lambda\\$([^$]+)\\$\\d$", "$1"));
              } catch (NoClassDefFoundError | ClassNotFoundException | NoSuchMethodException e) {
                return null;
              }
            })
        .filter(Objects::nonNull)
        .map(TestMethod::create)
        .filter(Optional::isPresent)
        .findFirst()
        .orElseThrow()
        .orElseThrow();
  }

  /**
   * Finds the source path of the test method, making the following assumptions:
   *
   * <ul>
   *   <li>the test class' name is also the name of the source file,
   *   <li>the base package is at most 10 levels deep,
   *   <li>the filename extension of the file is <code>java</code>, <code>kt</code>, <code>groovy
   *       </code>, or <code>scala</code>
   * </ul>
   *
   * @param testMethod the test {@link Method}
   * @return the {@link Path} to the source file containing the given testMethod
   */
  public static Path findTestSourcePath(Method testMethod) {
    Class<?> declaringClass = testMethod.getDeclaringClass();
    int packageDepth = declaringClass.getPackageName().split("\\.").length;
    String sourceSetName =
        Path.of(declaringClass.getProtectionDomain().getCodeSource().getLocation().getPath())
            .getFileName()
            .toString();
    String packagePath = declaringClass.getPackageName().replace(".", "/");
    String pathRegex =
        "(?!build|target).*%s.*/%s/%s\\.(java|kt|groovy|scala)$"
            .formatted(sourceSetName, packagePath, declaringClass.getSimpleName());
    try (Stream<Path> pathStream =
        Files.find(
            Path.of(""),
            packageDepth + 10,
            (path, attributes) ->
                attributes.isRegularFile() && path.normalize().toString().matches(pathRegex))) {
      return pathStream
          .findFirst()
          .map(Path::toAbsolutePath)
          .map(Path::normalize)
          .orElseThrow(() -> new FileApproverError("Could not locate test source file"));
    } catch (IOException e) {
      throw new FileApproverError("Could not traverse code directory", e);
    }
  }
}
