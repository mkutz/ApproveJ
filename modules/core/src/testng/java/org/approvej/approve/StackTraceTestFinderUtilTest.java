package org.approvej.approve;

import static org.testng.Assert.assertEquals;

import java.lang.reflect.Method;
import org.testng.annotations.Test;

public class StackTraceTestFinderUtilTest {

  @Test
  void currentTestMethod() throws NoSuchMethodException {
    Method thisMethod = getClass().getDeclaredMethod("currentTestMethod");

    TestMethod currentTestMethod = StackTraceTestFinderUtil.currentTestMethod();

    assertEquals(currentTestMethod.method(), thisMethod);
    assertEquals(currentTestMethod.testClass(), getClass());
    assertEquals(currentTestMethod.testCaseName(), thisMethod.getName());
  }
}
