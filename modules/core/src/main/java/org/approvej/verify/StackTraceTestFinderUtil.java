package org.approvej.verify;

import static java.util.Arrays.stream;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

/** Utility class to find the current test method using the stack trace. */
class StackTraceTestFinderUtil {

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
}
