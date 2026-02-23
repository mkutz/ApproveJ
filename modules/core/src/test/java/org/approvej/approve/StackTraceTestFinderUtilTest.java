package org.approvej.approve;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
  @ValueSource(strings = {"first"})
  void currentTestMethod_parameterized(String parameter) throws NoSuchMethodException {
    Method thisMethod =
        getClass().getDeclaredMethod("currentTestMethod_parameterized", parameter.getClass());

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

    assertThat(testSourcePath).isEqualTo(thisTestSourcePath.normalize());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "build/spotless-clean/spotlessJava/java/test/org/approvej/approve/StackTraceTestFinderUtilTest.java",
        "target/spotless-clean/spotlessJava/java/test/org/approvej/approve/StackTraceTestFinderUtilTest.java",
        "bin/test/org/approvej/approve/StackTraceTestFinderUtilTest.java",
        "out/test/org/approvej/approve/StackTraceTestFinderUtilTest.java",
        "other/test/java/org/approvej/approve/StackTraceTestFinderUtilTest.java",
      })
  void findTestSourcePath_duplicate_file(String wrongPath) throws IOException {
    Path wrongTestSourcePath = Path.of(wrongPath);
    wrongTestSourcePathsToCleanup.add(wrongTestSourcePath);
    createDirectories(wrongTestSourcePath.getParent());
    copy(thisTestSourcePath, wrongTestSourcePath);

    Path testSourcePath =
        StackTraceTestFinderUtil.findTestSourcePath(
            StackTraceTestFinderUtil.currentTestMethod().method());

    assertThat(testSourcePath).isEqualTo(thisTestSourcePath.normalize());
  }

  @Test
  void findTestSourcePath_no_source_file() throws NoSuchMethodException {
    Method method =
        org.assertj.core.api.Assertions.class.getDeclaredMethod("assertThat", boolean.class);

    assertThatThrownBy(() -> StackTraceTestFinderUtil.findTestSourcePath(method))
        .isInstanceOf(FileApproverError.class)
        .hasMessage("Could not locate test source file");
  }

  @Test
  void findTestSourcePath_ambiguous() throws IOException {
    Path wrongTestSourcePath =
        Path.of("other/src/test/java/org/approvej/approve/StackTraceTestFinderUtilTest.java");
    wrongTestSourcePathsToCleanup.add(wrongTestSourcePath);
    createDirectories(wrongTestSourcePath.getParent());
    copy(thisTestSourcePath, wrongTestSourcePath);

    Method method = StackTraceTestFinderUtil.currentTestMethod().method();

    assertThatThrownBy(() -> StackTraceTestFinderUtil.findTestSourcePath(method))
        .isInstanceOf(FileApproverError.class)
        .hasMessageStartingWith("Found multiple test source files (");
  }
}
