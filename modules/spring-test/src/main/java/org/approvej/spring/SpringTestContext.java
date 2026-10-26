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
 * @param testClasses the test classes directly using the context, sorted by name; empty for a
 *     parent context that is only used in a context hierarchy
 * @param dirtiesContextUsages the {@link DirtiesContextUsage}s causing the context to be started
 *     again, including those of test classes using a child context
 */
@NullMarked
public record SpringTestContext(
    MergedContextConfiguration configuration,
    List<Class<?>> testClasses,
    List<DirtiesContextUsage> dirtiesContextUsages) {

  /**
   * Creates a new {@link SpringTestContext}.
   *
   * @param configuration the {@link MergedContextConfiguration} identifying the context
   * @param testClasses the test classes directly using the context
   * @param dirtiesContextUsages the {@link DirtiesContextUsage}s causing the context to be started
   *     again
   */
  public SpringTestContext {
    testClasses = List.copyOf(testClasses);
    dirtiesContextUsages = List.copyOf(dirtiesContextUsages);
  }
}
