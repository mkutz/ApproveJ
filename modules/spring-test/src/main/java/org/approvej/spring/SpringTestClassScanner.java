package org.approvej.spring;

import static org.springframework.core.annotation.MergedAnnotations.SearchStrategy.TYPE_HIERARCHY;
import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.annotation.Testable;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

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
          .filter(SpringTestClassScanner::isSpringTestClass)
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

  static boolean isSpringTestClass(Class<?> type) {
    return isRunnable(type) && usesSpringExtension(type) && containsTests(type);
  }

  private static boolean isRunnable(Class<?> type) {
    if (type.isInterface()
        || type.isAnnotation()
        || type.isEnum()
        || type.isRecord()
        || type.isLocalClass()
        || type.isAnonymousClass()
        || Modifier.isAbstract(type.getModifiers())
        || Modifier.isPrivate(type.getModifiers())) {
      return false;
    }
    return !type.isMemberClass()
        || Modifier.isStatic(type.getModifiers())
        || type.isAnnotationPresent(Nested.class);
  }

  private static boolean usesSpringExtension(Class<?> type) {
    return Stream.<@Nullable Class<?>>iterate(
            type,
            Objects::nonNull,
            current ->
                current.isAnnotationPresent(Nested.class) ? current.getEnclosingClass() : null)
        .anyMatch(
            current ->
                MergedAnnotations.from(current, TYPE_HIERARCHY).stream(ExtendWith.class)
                    .flatMap(extendWith -> Arrays.stream(extendWith.getClassArray("value")))
                    .anyMatch(SpringExtension.class::isAssignableFrom));
  }

  private static boolean containsTests(Class<?> type) {
    return Arrays.stream(ReflectionUtils.getUniqueDeclaredMethods(type))
            .anyMatch(method -> MergedAnnotations.from(method).isPresent(Testable.class))
        || Arrays.stream(type.getDeclaredClasses())
            .filter(nested -> nested.isAnnotationPresent(Nested.class))
            .anyMatch(SpringTestClassScanner::containsTests);
  }
}
