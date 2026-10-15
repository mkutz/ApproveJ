package org.approvej.spring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.context.BootstrapUtils;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContextAnnotationUtils;
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
    Map<MergedContextConfiguration, List<DirtiesContextUsage>> usagesByConfiguration =
        new LinkedHashMap<>();
    Arrays.stream(basePackages)
        .flatMap(basePackage -> scanner.scan(basePackage).stream())
        .distinct()
        .sorted(Comparator.comparing(Class::getName))
        .forEach(
            testClass -> {
              MergedContextConfiguration configuration = configuration(testClass);
              testClassesByConfiguration
                  .computeIfAbsent(configuration, key -> new ArrayList<>())
                  .add(testClass);
              for (MergedContextConfiguration parent = configuration.getParent();
                  parent != null;
                  parent = parent.getParent()) {
                testClassesByConfiguration.computeIfAbsent(parent, key -> new ArrayList<>());
              }
              dirtiesContextUsages(testClass)
                  .forEach(
                      usage ->
                          usagesByConfiguration
                              .computeIfAbsent(configuration, key -> new ArrayList<>())
                              .add(usage));
            });
    return new SpringTestContexts(
        testClassesByConfiguration.entrySet().stream()
            .map(
                entry ->
                    new SpringTestContext(
                        entry.getKey(),
                        entry.getValue(),
                        affectingUsages(entry.getKey(), usagesByConfiguration)))
            .toList());
  }

  /**
   * Determines the {@link DirtiesContextUsage}s that cause the given context to be closed.
   *
   * <p>Dirtying a context also closes its child contexts. With {@link HierarchyMode#EXHAUSTIVE},
   * the whole hierarchy below the root of the dirtied context is closed.
   */
  private static List<DirtiesContextUsage> affectingUsages(
      MergedContextConfiguration configuration,
      Map<MergedContextConfiguration, List<DirtiesContextUsage>> usagesByConfiguration) {
    return usagesByConfiguration.entrySet().stream()
        .flatMap(
            entry ->
                entry.getValue().stream()
                    .filter(
                        usage ->
                            isSelfOrAncestor(
                                usage.annotation().hierarchyMode() == HierarchyMode.EXHAUSTIVE
                                    ? root(entry.getKey())
                                    : entry.getKey(),
                                configuration)))
        .toList();
  }

  private static MergedContextConfiguration root(MergedContextConfiguration configuration) {
    MergedContextConfiguration root = configuration;
    while (root.getParent() != null) {
      root = root.getParent();
    }
    return root;
  }

  private static boolean isSelfOrAncestor(
      MergedContextConfiguration candidate, MergedContextConfiguration configuration) {
    for (MergedContextConfiguration current = configuration;
        current != null;
        current = current.getParent()) {
      if (current.equals(candidate)) {
        return true;
      }
    }
    return false;
  }

  private static Stream<DirtiesContextUsage> dirtiesContextUsages(Class<?> testClass) {
    DirtiesContext classAnnotation =
        TestContextAnnotationUtils.findMergedAnnotation(testClass, DirtiesContext.class);
    return Stream.concat(
        Stream.ofNullable(classAnnotation)
            .map(annotation -> new DirtiesContextUsage(testClass, null, annotation)),
        SpringTestClassScanner.testMethods(testClass)
            .sorted(Comparator.comparing(Method::getName))
            .flatMap(
                method ->
                    Stream.ofNullable(
                            AnnotatedElementUtils.findMergedAnnotation(
                                method, DirtiesContext.class))
                        .map(
                            annotation -> new DirtiesContextUsage(testClass, method, annotation))));
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
   * Returns the distinct contexts, including the parent contexts of context hierarchies, in the
   * order they were found.
   *
   * @return the distinct {@link SpringTestContext}s
   */
  public List<SpringTestContext> contexts() {
    return contexts;
  }
}
