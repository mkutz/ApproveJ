package org.approvej.approve;

import org.jspecify.annotations.NullMarked;

/**
 * Identifies the test case that is currently being executed, independent of the testing framework.
 *
 * <p>Used by {@link CurrentTestLocator} implementations for frameworks whose test cases cannot be
 * found on the stack trace (e.g. Kotest, where tests are lambdas rather than annotated methods).
 *
 * @param testClass the class containing the test case (e.g. the spec class)
 * @param testCaseName the name of the test case, used as the base of the approved and received
 *     filenames
 */
@NullMarked
public record TestLocation(Class<?> testClass, String testCaseName) {}
