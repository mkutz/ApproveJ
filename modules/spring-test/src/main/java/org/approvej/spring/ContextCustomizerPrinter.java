package org.approvej.spring;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Prints a {@link ContextCustomizer} as stable, human-readable lines.
 *
 * <p>Most {@link ContextCustomizer}s do not implement a meaningful {@link Object#toString()}, so
 * the relevant state of well-known ones is read reflectively. Spring Boot's customizers are matched
 * by name, so Spring Boot is not required. Customizers without any state (apart from loggers) are
 * omitted, as their presence is implied by other details. Other customizers are printed by their
 * class name.
 */
@NullMarked
final class ContextCustomizerPrinter {

  private static final Set<String> LOGGER_TYPES =
      Set.of(
          "org.apache.commons.logging.Log",
          "org.slf4j.Logger",
          "java.lang.System$Logger",
          "java.util.logging.Logger");

  private ContextCustomizerPrinter() {}

  static Stream<String> print(ContextCustomizer customizer) {
    try {
      return switch (customizer.getClass().getName()) {
        case "org.springframework.boot.test.context.SpringBootTestAnnotation" ->
            springBootTest(customizer);
        case "org.springframework.boot.test.context.PropertyMappingContextCustomizer" ->
            propertyMappings(customizer);
        case "org.springframework.boot.test.context.ImportsContextCustomizer" ->
            imports(customizer);
        case "org.springframework.boot.test.context.filter.annotation"
                + ".TypeExcludeFiltersContextCustomizer" ->
            typeExcludeFilters(customizer);
        case "org.springframework.test.context.support.DynamicPropertiesContextCustomizer" ->
            dynamicPropertySources(customizer);
        case "org.springframework.test.context.bean.override.BeanOverrideContextCustomizer" ->
            BeanOverridePrinter.print(fieldValue(customizer, "handlers", Collection.class));
        default -> other(customizer);
      };
    } catch (RuntimeException e) {
      return Stream.of(customizer.getClass().getName());
    }
  }

  private static Stream<String> springBootTest(ContextCustomizer customizer) {
    StringBuilder attributes =
        new StringBuilder(
            "webEnvironment=%s".formatted(fieldValue(customizer, "webEnvironment", Enum.class)));
    String[] args = fieldValue(customizer, "args", String[].class);
    if (args.length > 0) {
      attributes.append(", args=%s".formatted(Arrays.toString(args)));
    }
    Enum<?> useMainMethod = fieldValue(customizer, "useMainMethod", Enum.class);
    if (!useMainMethod.name().equals("NEVER")) {
      attributes.append(", useMainMethod=%s".formatted(useMainMethod));
    }
    return Stream.of("@SpringBootTest(%s)".formatted(attributes));
  }

  private static Stream<String> propertyMappings(ContextCustomizer customizer) {
    EnumerablePropertySource<?> propertySource =
        fieldValue(customizer, "propertySource", EnumerablePropertySource.class);
    return Arrays.stream(propertySource.getPropertyNames())
        .sorted()
        .map(
            name ->
                "property mapping %s=%s"
                    .formatted(
                        name, ObjectUtils.nullSafeToString(propertySource.getProperty(name))));
  }

  private static Stream<String> imports(ContextCustomizer customizer) {
    Object key = fieldValue(customizer, "key", Object.class);
    return fieldValue(key, "key", Collection.class).stream()
        .map(
            imported ->
                imported instanceof String className
                    ? "@Import %s".formatted(className)
                    : "import key %s".formatted(imported));
  }

  private static Stream<String> typeExcludeFilters(ContextCustomizer customizer) {
    return fieldValue(customizer, "filters", Collection.class).stream()
        .map(filter -> "type exclude filter %s".formatted(filter.getClass().getName()));
  }

  private static Stream<String> dynamicPropertySources(ContextCustomizer customizer) {
    Collection<?> methods = fieldValue(customizer, "methods", Collection.class);
    return methods.stream()
        .map(Method.class::cast)
        .map(
            method ->
                "@DynamicPropertySource %s.%s"
                    .formatted(method.getDeclaringClass().getName(), method.getName()));
  }

  private static Stream<String> other(ContextCustomizer customizer) {
    return hasState(customizer.getClass())
        ? Stream.of(customizer.getClass().getName())
        : Stream.empty();
  }

  private static boolean hasState(Class<?> type) {
    return Stream.<@Nullable Class<?>>iterate(
            type, current -> current != null && current != Object.class, Class::getSuperclass)
        .flatMap(current -> Arrays.stream(current.getDeclaredFields()))
        .anyMatch(
            field ->
                !Modifier.isStatic(field.getModifiers())
                    && !field.isSynthetic()
                    && !LOGGER_TYPES.contains(field.getType().getName()));
  }

  static <T> T fieldValue(Object target, String name, Class<T> type) {
    Field field = ReflectionUtils.findField(target.getClass(), name);
    if (field == null) {
      throw new IllegalStateException(
          "No field %s in %s".formatted(name, target.getClass().getName()));
    }
    ReflectionUtils.makeAccessible(field);
    return type.cast(ReflectionUtils.getField(field, target));
  }
}
