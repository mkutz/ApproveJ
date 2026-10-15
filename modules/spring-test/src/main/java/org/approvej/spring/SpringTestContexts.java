package org.approvej.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.springframework.test.context.BootstrapUtils;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.util.ClassUtils;

/**
 * All distinct Spring test application contexts used by the test classes in a set of packages.
 *
 * <p>Approve this to notice when a new test application context is introduced, as each one slows
 * down the test suite.
 *
 * <p>The contexts are determined statically: no test is run and no context is started.
 */
@NullMarked
public final class SpringTestContexts {

  private final List<SpringTestContext> contexts;

  private SpringTestContexts(List<SpringTestContext> contexts) {
    this.contexts = List.copyOf(contexts);
  }

  /**
   * Determines the distinct Spring test application contexts of all Spring test classes in the
   * given packages and their sub-packages.
   *
   * @param basePackages the names of the packages to scan
   * @return the {@link SpringTestContexts} found
   */
  public static SpringTestContexts springTestContexts(String... basePackages) {
    SpringTestClassScanner scanner =
        new SpringTestClassScanner(Objects.requireNonNull(ClassUtils.getDefaultClassLoader()));
    Map<MergedContextConfiguration, List<Class<?>>> testClassesByConfiguration =
        new LinkedHashMap<>();
    Arrays.stream(basePackages)
        .flatMap(basePackage -> scanner.scan(basePackage).stream())
        .distinct()
        .sorted(Comparator.comparing(Class::getName))
        .forEach(
            testClass ->
                testClassesByConfiguration
                    .computeIfAbsent(configuration(testClass), configuration -> new ArrayList<>())
                    .add(testClass));
    return new SpringTestContexts(
        testClassesByConfiguration.entrySet().stream()
            .map(entry -> new SpringTestContext(entry.getKey(), entry.getValue()))
            .toList());
  }

  private static MergedContextConfiguration configuration(Class<?> testClass) {
    try {
      return BootstrapUtils.resolveTestContextBootstrapper(testClass)
          .buildMergedContextConfiguration();
    } catch (RuntimeException e) {
      throw new IllegalStateException(
          "Failed to determine the context configuration of %s".formatted(testClass.getName()), e);
    }
  }

  /**
   * Returns the distinct contexts in the order of their first test class.
   *
   * @return the distinct {@link SpringTestContext}s
   */
  public List<SpringTestContext> contexts() {
    return contexts;
  }
}
