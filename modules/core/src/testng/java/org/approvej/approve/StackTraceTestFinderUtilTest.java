package org.approvej.approve;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

public class StackTraceTestFinderUtilTest {

  private final Path thisTestSourcePath =
      Path.of("src/testng/java/org/approvej/approve/StackTraceTestFinderUtilTest.java");
  private final List<Path> wrongTestSourcePathsToCleanup = new ArrayList<>();

  @AfterTest
  void cleanup() throws IOException {
    for (Path path : wrongTestSourcePathsToCleanup) {
      Files.deleteIfExists(path);
    }
  }

  @Test
  void currentTestMethod() throws NoSuchMethodException {
    Method thisMethod = getClass().getDeclaredMethod("currentTestMethod");

    TestMethod currentTestMethod = StackTraceTestFinderUtil.currentTestMethod();

    assertEquals(currentTestMethod.method(), thisMethod);
    assertEquals(currentTestMethod.testClass(), getClass());
    assertEquals(currentTestMethod.testCaseName(), thisMethod.getName());
  }

  @Test
  void findTestSourcePath() {
    Path testSourcePath =
        StackTraceTestFinderUtil.findTestSourcePath(
            StackTraceTestFinderUtil.currentTestMethod().method());

    assertEquals(testSourcePath, thisTestSourcePath.toAbsolutePath().normalize());
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

    assertEquals(testSourcePath, thisTestSourcePath.toAbsolutePath().normalize());
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

    assertEquals(testSourcePath, thisTestSourcePath.toAbsolutePath().normalize());
  }
}
