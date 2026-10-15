package org.approvej.spring;

import java.lang.reflect.Method;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.test.annotation.DirtiesContext;

/**
 * A usage of {@link DirtiesContext} that causes a Spring test application context to be closed and
 * started again.
 *
 * @param testClass the test class the annotation applies to
 * @param testMethod the test method the annotation applies to, or {@code null} if it applies to the
 *     whole test class
 * @param annotation the {@link DirtiesContext} annotation
 */
@NullMarked
public record DirtiesContextUsage(
    Class<?> testClass, @Nullable Method testMethod, DirtiesContext annotation) {}
