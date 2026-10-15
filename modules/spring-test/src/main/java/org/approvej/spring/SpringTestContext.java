package org.approvej.spring;

import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.springframework.test.context.MergedContextConfiguration;

/**
 * A distinct Spring test application context and the test classes sharing it.
 *
 * <p>Spring caches test application contexts by their {@link MergedContextConfiguration}. All test
 * classes with an equal configuration share one context, each additional distinct configuration
 * causes another context to be started.
 *
 * @param configuration the {@link MergedContextConfiguration} identifying the context
 * @param testClasses the test classes using the context, sorted by name
 */
@NullMarked
public record SpringTestContext(
    MergedContextConfiguration configuration, List<Class<?>> testClasses) {

  /**
   * Creates a new {@link SpringTestContext}.
   *
   * @param configuration the {@link MergedContextConfiguration} identifying the context
   * @param testClasses the test classes using the context
   */
  public SpringTestContext {
    testClasses = List.copyOf(testClasses);
  }
}
