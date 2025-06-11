package org.approvej.approve;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class StackTraceTestFinderUtilTest {

  private final Path thisTestSourcePath =
      Path.of("src/test/java/org/approvej/approve/StackTraceTestFinderUtilTest.java");
  private final List<Path> wrongTestSourcePathsToCleanup = new ArrayList<>();

  @AfterEach
  void cleanup() throws IOException {
    for (Path path : wrongTestSourcePathsToCleanup) {
      Files.deleteIfExists(path);
    }
  }

  @Test
  void currentTestMethod() throws NoSuchMethodException {
    Method thisMethod = getClass().getDeclaredMethod("currentTestMethod");

    TestMethod currentTestMethod = StackTraceTestFinderUtil.currentTestMethod();

    assertThat(currentTestMethod.method()).isEqualTo(thisMethod);
    assertThat(currentTestMethod.testClass()).isEqualTo(thisMethod.getDeclaringClass());
    assertThat(currentTestMethod.testCaseName()).isEqualTo(thisMethod.getName());
  }

  @ParameterizedTest
  @ValueSource(strings = {"a", "b", "c"})
  void currentTestMethod_parameterized_test() throws NoSuchMethodException {
    Method thisMethod = getClass().getDeclaredMethod("currentTestMethod_parameterized_test");

    TestMethod currentTestMethod = StackTraceTestFinderUtil.currentTestMethod();

    assertThat(currentTestMethod.method()).isEqualTo(thisMethod);
    assertThat(currentTestMethod.testClass()).isEqualTo(thisMethod.getDeclaringClass());
    assertThat(currentTestMethod.testCaseName()).isEqualTo(thisMethod.getName());
  }

  @Test
  void findTestSourcePath() {
    Path testSourcePath =
        StackTraceTestFinderUtil.findTestSourcePath(
            StackTraceTestFinderUtil.currentTestMethod().method());

    assertThat(testSourcePath).isEqualTo(thisTestSourcePath.toAbsolutePath().normalize());
  }

  @Test
  void findTestSourcePath_file_in_build() throws IOException {
    Path wrongTestSourcePath =
        Path.of(
            "build/spotless-clean/spotlessJava/java/test/org/approvej/approve/StackTraceTestFinderUtilTest.java");
    wrongTestSourcePathsToCleanup.add(wrongTestSourcePath);
    createDirectories(wrongTestSourcePath.getParent());
    copy(thisTestSourcePath, wrongTestSourcePath);

    Path testSourcePath =
        StackTraceTestFinderUtil.findTestSourcePath(
            StackTraceTestFinderUtil.currentTestMethod().method());

    assertThat(testSourcePath).isEqualTo(thisTestSourcePath.toAbsolutePath().normalize());
  }

  @Test
  void findTestSourcePath_file_in_target() throws IOException {
    Path wrongTestSourcePath =
        Path.of(
            "target/spotless-clean/spotlessJava/java/test/org/approvej/approve/StackTraceTestFinderUtilTest.java");
    wrongTestSourcePathsToCleanup.add(wrongTestSourcePath);
    createDirectories(wrongTestSourcePath.getParent());
    copy(thisTestSourcePath, wrongTestSourcePath);

    Path testSourcePath =
        StackTraceTestFinderUtil.findTestSourcePath(
            StackTraceTestFinderUtil.currentTestMethod().method());

    assertThat(testSourcePath).isEqualTo(thisTestSourcePath.toAbsolutePath().normalize());
  }
}
