package org.approvej.approve;

import java.util.Optional;
import org.jspecify.annotations.NullMarked;

/**
 * Locates the currently executing test case for testing frameworks that ApproveJ cannot find on the
 * stack trace.
 *
 * <p>ApproveJ's default mechanism ({@link StackTraceTestFinderUtil#currentTestMethod()}) inspects
 * the stack trace for an annotated test method. This works for frameworks like JUnit, TestNG, and
 * Spock, where every test case is a distinct method. It does not work for frameworks like Kotest,
 * where test cases are lambdas registered at runtime and the test name only exists as data.
 *
 * <p>Implementations are discovered via the Java {@link java.util.ServiceLoader} mechanism. To
 * register one, add its fully-qualified class name to {@code
 * META-INF/services/org.approvej.approve.CurrentTestLocator}.
 *
 * @see PathProviders#nextToTest()
 */
@NullMarked
public interface CurrentTestLocator {

  /**
   * Returns the currently executing test case, if this locator's framework is actively running one.
   *
   * @return the current {@link TestLocation}, or an empty {@link Optional} if no test of this
   *     locator's framework is currently running
   */
  Optional<TestLocation> currentTest();
}
