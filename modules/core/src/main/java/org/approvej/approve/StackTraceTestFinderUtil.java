package org.approvej.approve;

import static java.util.Arrays.stream;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
        .flatMap(
            element -> {
              try {
                String methodName =
                    element.getMethodName().replaceAll("^lambda\\$([^$]+)\\$\\d$", "$1");
                return stream(Class.forName(element.getClassName()).getDeclaredMethods())
                    .filter(method -> method.getName().equals(methodName));
              } catch (NoClassDefFoundError | ClassNotFoundException e) {
                return Stream.empty();
              }
            })
        .map(TestMethod::create)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst()
        .orElseThrow(() -> new TestMethodNotFoundInStackTraceError(stackTrace));
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
    String sourceSetName;
    try {
      URI locationUri = declaringClass.getProtectionDomain().getCodeSource().getLocation().toURI();
      sourceSetName = Path.of(locationUri).getFileName().toString().replace("-classes", "");
    } catch (URISyntaxException e) {
      throw new FileApproverError("Could not parse code source location", e);
    }
    Class<?> topLevelClass = declaringClass;
    while (topLevelClass.getEnclosingClass() != null) {
      topLevelClass = topLevelClass.getEnclosingClass();
    }
    String packagePath = declaringClass.getPackageName().replace(".", "/");
    String pathRegex =
        "(?!(?:build|target|bin|out)/).*%s.*/%s/%s\\.(java|kt|groovy|scala)$"
            .formatted(sourceSetName, packagePath, topLevelClass.getSimpleName());
    try (Stream<Path> pathStream =
        Files.find(
            Path.of(""),
            packageDepth + 10,
            (path, attributes) ->
                attributes.isRegularFile()
                    && path.normalize().toString().replace('\\', '/').matches(pathRegex))) {
      List<Path> matches = pathStream.map(Path::normalize).toList();
      return switch (matches.size()) {
        case 0 -> throw new FileApproverError("Could not locate test source file");
        case 1 -> matches.getFirst();
        default -> {
          List<Path> srcMatches =
              matches.stream().filter(path -> path.toString().contains("src")).toList();
          if (srcMatches.size() == 1) {
            yield srcMatches.getFirst();
          }
          throw new FileApproverError(
              "Found multiple test source files (%d contain 'src'): %s"
                  .formatted(srcMatches.size(), matches));
        }
      };
    } catch (IOException e) {
      throw new FileApproverError("Could not traverse code directory", e);
    }
  }
}
