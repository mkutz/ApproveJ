package org.approvej.approve

import spock.lang.Specification

import java.nio.file.Path

import static java.nio.file.Files.copy
import static java.nio.file.Files.createDirectories
import static java.nio.file.Files.delete

class StackTraceTestFinderUtilSpockSpec extends Specification {

  private final Path thisTestSourcePath = Path.of("src/spock/groovy/org/approvej/approve/StackTraceTestFinderUtilSpockSpec.groovy")
  private final List<Path> wrongTestSourcePathsToCleanup = []

  def 'currentTestMethod'() {
    when:
    TestMethod currentTestMethod = StackTraceTestFinderUtil.currentTestMethod()

    then:
    verifyAll(currentTestMethod) {
      method()
      testClass() == StackTraceTestFinderUtilSpockSpec
      testCaseName() == 'currentTestMethod'
    }
  }

  def 'findTestSourcePath'() {
    when:
    Path testSourcePath =
        StackTraceTestFinderUtil.findTestSourcePath(
        StackTraceTestFinderUtil.currentTestMethod().method())

    then:
    testSourcePath == thisTestSourcePath.normalize()
  }

  def 'findTestSourcePath_file_in_build'() {
    given:
    Path wrongTestSourcePath =
        Path.of(
        "build/spotless-clean/spotlessGroovy/groovy/test/org/approvej/approve/StackTraceTestFinderUtilSpec.groovy")
    wrongTestSourcePathsToCleanup.add(wrongTestSourcePath)
    createDirectories(wrongTestSourcePath.getParent())
    copy(thisTestSourcePath, wrongTestSourcePath)

    when:
    Path testSourcePath =
        StackTraceTestFinderUtil.findTestSourcePath(
        StackTraceTestFinderUtil.currentTestMethod().method())

    then:
    testSourcePath == thisTestSourcePath.normalize()

    cleanup:
    delete(wrongTestSourcePath)
  }

  def 'findTestSourcePath_file_in_target'() {
    given:
    Path wrongTestSourcePath =
        Path.of(
        "target/spotless-clean/spotlessGroovy/groovy/test/org/approvej/approve/StackTraceTestFinderUtilSpec.groovy")
    wrongTestSourcePathsToCleanup.add(wrongTestSourcePath)
    createDirectories(wrongTestSourcePath.getParent())
    copy(thisTestSourcePath, wrongTestSourcePath)

    when:
    Path testSourcePath = StackTraceTestFinderUtil.findTestSourcePath(StackTraceTestFinderUtil.currentTestMethod().method())

    then:
    testSourcePath == thisTestSourcePath.normalize()

    cleanup:
    delete(wrongTestSourcePath)
  }
}
