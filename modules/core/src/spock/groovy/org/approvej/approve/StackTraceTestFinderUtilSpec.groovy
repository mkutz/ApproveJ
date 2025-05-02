package org.approvej.approve

import spock.lang.Specification

class StackTraceTestFinderUtilSpec extends Specification {

  def 'currentTestMethod'() {
    when:
    TestMethod currentTestMethod = StackTraceTestFinderUtil.currentTestMethod()

    then:
    verifyAll(currentTestMethod) {
      method()
      testClass() == StackTraceTestFinderUtilSpec
      testCaseName() == 'currentTestMethod'
    }
  }
}
