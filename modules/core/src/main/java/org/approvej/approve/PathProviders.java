package org.approvej.approve;

import static java.util.Objects.requireNonNullElse;
import static org.approvej.approve.PathProvider.APPROVED;
import static org.approvej.approve.StackTraceTestFinderUtil.currentTestMethod;
import static org.approvej.approve.StackTraceTestFinderUtil.findTestSourcePath;
import static org.approvej.print.PrintFormat.DEFAULT_FILENAME_EXTENSION;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.NullMarked;

/** Collection of static methods to create {@link PathProvider} instances. */
@NullMarked
public final class PathProviders {

  private static final List<CurrentTestLocator> TEST_LOCATORS =
      ServiceLoader.load(CurrentTestLocator.class).stream()
          .map(ServiceLoader.Provider::get)
          .toList();

  private PathProviders() {}

  private static Optional<TestLocation> currentTestLocation() {
    return TEST_LOCATORS.stream()
        .map(CurrentTestLocator::currentTest)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  /**
   * Replaces characters that are illegal in filenames on common file systems with an underscore.
   * Test names of frameworks like Kotest are arbitrary strings (e.g. {@code "GET /article"}), as
   * opposed to identifier-safe method names.
   */
  private static String sanitizeFilename(String name) {
    return name.replaceAll("[\\\\/:*?\"<>|\\x00-\\x1F]", "_").strip();
  }

  private static String nestedSimpleName(Class<?> clazz) {
    java.util.ArrayDeque<String> parts = new java.util.ArrayDeque<>();
    parts.addFirst(clazz.getSimpleName());
    Class<?> enclosing = clazz.getEnclosingClass();
    while (enclosing != null) {
      parts.addFirst(enclosing.getSimpleName());
      enclosing = enclosing.getEnclosingClass();
    }
    return String.join(".", parts);
  }

  /**
   * Creates a new {@link PathProvider} that uses the given approved {@link Path}.
   *
   * @param approvedPath the {@link Path} to the approved file
   * @return a new {@link PathProvider}
   */
  public static PathProvider approvedPath(Path approvedPath) {
    Path directory = approvedPath.getParent();
    String approvedFilename = approvedPath.getFileName().toString();
    Matcher matcher =
        Pattern.compile("(?<base>.+?)(-(?<label>" + APPROVED + "))?(?:\\.(?<extension>[^.]*))?$")
            .matcher(approvedFilename);
    if (matcher.matches()) {
      String baseFilename = matcher.group("base");
      String approvedLabel = requireNonNullElse(matcher.group("label"), "");
      String filenameExtension = requireNonNullElse(matcher.group("extension"), "");
      return new PathProvider(directory, baseFilename, "", approvedLabel, filenameExtension);
    }
    return new PathProvider(directory, approvedFilename, "", "", "");
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
   * Creates a {@link PathProvider} that uses a stack trace to determine the paths of the approved
   * and received files.
   *
   * @return a new {@link PathProvider}
   */
  public static PathProvider nextToTest() {
    Optional<TestLocation> currentTestLocation = currentTestLocation();
    if (currentTestLocation.isPresent()) {
      TestLocation testLocation = currentTestLocation.get();
      Path directory = findTestSourcePath(testLocation.testClass()).getParent();
      String baseFilename =
          "%s-%s"
              .formatted(
                  nestedSimpleName(testLocation.testClass()),
                  sanitizeFilename(testLocation.testCaseName()));
      return new PathProvider(directory, baseFilename, "", APPROVED, DEFAULT_FILENAME_EXTENSION);
    }

    TestMethod testMethod = currentTestMethod();

    Path directory = findTestSourcePath(testMethod.method()).getParent();
    String baseFilename =
        "%s-%s".formatted(nestedSimpleName(testMethod.testClass()), testMethod.testCaseName());

    return new PathProvider(directory, baseFilename, "", APPROVED, DEFAULT_FILENAME_EXTENSION);
  }

  /**
   * Creates a {@link PathProvider} that uses a stack trace to determine the paths of the approved
   * and received files in a subdirectory named after the current test class. For nested or inner
   * classes, the subdirectory name includes enclosing class names separated by dots (e.g. {@code
   * OuterTest.InnerTest}).
   *
   * @return a new {@link PathProvider}
   */
  public static PathProvider nextToTestInSubdirectory() {
    Optional<TestLocation> currentTestLocation = currentTestLocation();
    if (currentTestLocation.isPresent()) {
      TestLocation testLocation = currentTestLocation.get();
      Path directory =
          findTestSourcePath(testLocation.testClass())
              .getParent()
              .resolve(nestedSimpleName(testLocation.testClass()));
      String baseFilename = sanitizeFilename(testLocation.testCaseName());
      return new PathProvider(directory, baseFilename, "", APPROVED, DEFAULT_FILENAME_EXTENSION);
    }

    TestMethod testMethod = currentTestMethod();

    Path directory =
        findTestSourcePath(testMethod.method())
            .getParent()
            .resolve(nestedSimpleName(testMethod.testClass()));
    String baseFilename = testMethod.method().getName();

    return new PathProvider(directory, baseFilename, "", APPROVED, DEFAULT_FILENAME_EXTENSION);
  }
}
