package org.approvej.approve;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class StackTraceTestFinderUtilTest {

  @Test
  void currentTestMethod() throws NoSuchMethodException {
    Method thisMethod = getClass().getDeclaredMethod("currentTestMethod");

    TestMethod currentTestMethod = StackTraceTestFinderUtil.currentTestMethod();

    assertThat(currentTestMethod.method()).isEqualTo(thisMethod);
    assertThat(currentTestMethod.testClass()).isEqualTo(thisMethod.getDeclaringClass());
    assertThat(currentTestMethod.testCaseName()).isEqualTo(thisMethod.getName());
  }
}
