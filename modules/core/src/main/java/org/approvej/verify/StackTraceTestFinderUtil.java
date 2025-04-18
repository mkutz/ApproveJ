package org.approvej.verify;

import static java.util.Arrays.stream;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

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
  static Method currentTestMethod() {
    return stream(Thread.currentThread().getStackTrace())
        .map(
            element -> {
              try {
                return Class.forName(element.getClassName())
                    .getDeclaredMethod(element.getMethodName());
              } catch (ClassNotFoundException | NoSuchMethodException e) {
                return null;
              }
            })
        .filter(method -> method != null && method.isAnnotationPresent(Test.class))
        .findFirst()
        .orElseThrow();
  }

  /**
   * Finds the source path of the test method, making the following assumptions:
   *
   * <ul>
   *   <li>the test class' name is also the name of the source file,
   *   <li>the file is at most 10 levels deep,
   *   <li>the filename extension of the file is <code>java</code>, <code>kt</code>, or <code>groovy
   *       </code>
   * </ul>
   *
   * @param testMethod the test {@link Method}
   * @return the {@link Path} to the source file containing the given testMethod
   */
  public static Path findTestSourcePath(Method testMethod) {
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
}
