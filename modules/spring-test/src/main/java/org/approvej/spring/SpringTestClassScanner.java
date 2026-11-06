package org.approvej.spring;

import static org.springframework.core.annotation.MergedAnnotations.SearchStrategy.TYPE_HIERARCHY;
import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.annotation.Testable;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ClassUtils;

/**
 * Finds all JUnit Jupiter test classes in a package that use the {@link SpringExtension} and
 * therefore load a Spring application context.
 */
@NullMarked
final class SpringTestClassScanner {

  private final ClassLoader classLoader;
  private final ResourcePatternResolver resourcePatternResolver;
  private final MetadataReaderFactory metadataReaderFactory;

  SpringTestClassScanner(ClassLoader classLoader) {
    this.classLoader = classLoader;
    this.resourcePatternResolver = new PathMatchingResourcePatternResolver(classLoader);
    this.metadataReaderFactory = new CachingMetadataReaderFactory(classLoader);
  }

  /**
   * Scans the given package and its sub-packages for Spring test classes.
   *
   * @param basePackage the name of the package to scan
   * @return the found Spring test classes, sorted by name
   */
  List<Class<?>> scan(String basePackage) {
    String pattern =
        "%s%s/**/*.class"
            .formatted(
                CLASSPATH_ALL_URL_PREFIX, ClassUtils.convertClassNameToResourcePath(basePackage));
    try {
      return Arrays.stream(resourcePatternResolver.getResources(pattern))
          .map(this::className)
          .distinct()
          .map(this::loadClass)
          .flatMap(Optional::stream)
          .flatMap(SpringTestClassScanner::springTestClasses)
          .distinct()
          .sorted(Comparator.comparing(Class::getName))
          .toList();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to scan package %s".formatted(basePackage), e);
    }
  }

  private String className(Resource resource) {
    try {
      return metadataReaderFactory.getMetadataReader(resource).getClassMetadata().getClassName();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read class file %s".formatted(resource), e);
    }
  }

  private Optional<Class<?>> loadClass(String className) {
    try {
      return Optional.of(Class.forName(className, false, classLoader));
    } catch (ClassNotFoundException | LinkageError e) {
      return Optional.empty();
    }
  }

  /**
   * Finds the given class and its nested test classes that load a Spring application context.
   *
   * <p>Like JUnit Jupiter, nested test classes are discovered through their runnable enclosing
   * classes, including nested classes inherited from superclasses.
   */
  static Stream<Class<?>> springTestClasses(Class<?> type) {
    return isRunnable(type) ? springTestClasses(type, false) : Stream.empty();
  }

  private static Stream<Class<?>> springTestClasses(
      Class<?> type, boolean springExtensionRegisteredByEnclosingClass) {
    boolean springExtensionRegistered =
        springExtensionRegisteredByEnclosingClass || registersSpringExtension(type);
    boolean loadsContext =
        springExtensionRegistered
            ? containsTests(type)
            : testMethods(type).anyMatch(SpringTestClassScanner::registersSpringExtension);
    return Stream.concat(
        loadsContext ? Stream.of(type) : Stream.empty(),
        nestedTestClasses(type)
            .flatMap(nested -> springTestClasses(nested, springExtensionRegistered)));
  }

  private static boolean isRunnable(Class<?> type) {
    return !type.isInterface()
        && !type.isAnnotation()
        && !type.isEnum()
        && !type.isLocalClass()
        && !type.isAnonymousClass()
        && !Modifier.isAbstract(type.getModifiers())
        && !Modifier.isPrivate(type.getModifiers())
        && (!type.isMemberClass() || Modifier.isStatic(type.getModifiers()));
  }

  private static boolean isNestedTestClass(Class<?> type) {
    return type.isMemberClass()
        && !Modifier.isStatic(type.getModifiers())
        && !Modifier.isAbstract(type.getModifiers())
        && !Modifier.isPrivate(type.getModifiers())
        && type.isAnnotationPresent(Nested.class);
  }

  private static Stream<Class<?>> nestedTestClasses(Class<?> type) {
    return ReflectionSupport.streamNestedClasses(type, SpringTestClassScanner::isNestedTestClass);
  }

  private static boolean registersSpringExtension(AnnotatedElement element) {
    return MergedAnnotations.from(element, TYPE_HIERARCHY).stream(ExtendWith.class)
        .flatMap(extendWith -> Arrays.stream(extendWith.getClassArray("value")))
        .anyMatch(SpringExtension.class::isAssignableFrom);
  }

  /**
   * Finds the test methods JUnit Jupiter executes for the given class, including inherited ones.
   */
  static Stream<Method> testMethods(Class<?> type) {
    return ReflectionSupport.streamMethods(
        type, SpringTestClassScanner::isTestMethod, HierarchyTraversalMode.TOP_DOWN);
  }

  private static boolean isTestMethod(Method method) {
    int modifiers = method.getModifiers();
    return !Modifier.isStatic(modifiers)
        && !Modifier.isPrivate(modifiers)
        && !Modifier.isAbstract(modifiers)
        && MergedAnnotations.from(method).isPresent(Testable.class);
  }

  private static boolean containsTests(Class<?> type) {
    return testMethods(type).findAny().isPresent()
        || nestedTestClasses(type).anyMatch(SpringTestClassScanner::containsTests);
  }
}
