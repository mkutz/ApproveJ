package org.approvej.spring;

import static java.util.stream.Collectors.joining;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.test.context.bean.override.BeanOverride;
import org.springframework.test.context.bean.override.BeanOverrideHandler;
import org.springframework.util.ObjectUtils;

/**
 * Prints the {@link BeanOverrideHandler}s of a bean override context customizer, e.g. for
 * {@code @MockitoBean}, {@code @MockitoSpyBean}, or {@code @TestBean}.
 *
 * <p>Everything that makes two handlers unequal, and so distinguishes their contexts, is printed:
 * the bean type, name, or field, the qualifier annotations of the field, the override strategy, and
 * all attributes of the bean override annotation that differ from their defaults.
 *
 * <p>This is a separate class so {@link BeanOverrideHandler} is only loaded if bean overrides are
 * used, as it does not exist in older Spring versions.
 */
@NullMarked
final class BeanOverridePrinter {

  /** Attributes that are already printed as the bean name. */
  private static final Set<String> BEAN_NAME_ATTRIBUTES = Set.of("value", "name");

  private BeanOverridePrinter() {}

  static Stream<String> print(Collection<?> handlers) {
    return handlers.stream().map(BeanOverrideHandler.class::cast).map(BeanOverridePrinter::print);
  }

  private static String print(BeanOverrideHandler handler) {
    StringBuilder line =
        new StringBuilder("%s %s".formatted(annotation(handler), handler.getBeanType()));
    String beanName = handler.getBeanName();
    Field field = handler.getField();
    if (beanName != null) {
      line.append(" name=%s".formatted(beanName));
    } else if (field != null) {
      line.append(" field=%s".formatted(field.getName()));
      qualifierAnnotations(field).forEach(qualifier -> line.append(" ").append(qualifier));
    }
    if (!handler.getContextName().isEmpty()) {
      line.append(" context=%s".formatted(handler.getContextName()));
    }
    line.append(" strategy=%s".formatted(handler.getStrategy()));
    if (field != null) {
      overrideAttributes(field).forEach(attribute -> line.append(" ").append(attribute));
    }
    return line.toString();
  }

  private static String annotation(BeanOverrideHandler handler) {
    return switch (handler.getClass().getSimpleName()) {
      case "MockitoBeanOverrideHandler" -> "@MockitoBean";
      case "MockitoSpyBeanOverrideHandler" -> "@MockitoSpyBean";
      case "TestBeanOverrideHandler" -> "@TestBean";
      default -> handler.getClass().getName();
    };
  }

  /**
   * Spring considers all annotations of an override field that are not bean override annotations to
   * be qualifiers.
   */
  private static Stream<String> qualifierAnnotations(Field field) {
    return Arrays.stream(field.getDeclaredAnnotations())
        .filter(annotation -> !isBeanOverrideAnnotation(annotation))
        .map(Annotation::toString)
        .sorted();
  }

  private static Stream<String> overrideAttributes(Field field) {
    return Arrays.stream(field.getDeclaredAnnotations())
        .filter(BeanOverridePrinter::isBeanOverrideAnnotation)
        .flatMap(BeanOverridePrinter::nonDefaultAttributes);
  }

  private static Stream<String> nonDefaultAttributes(Annotation annotation) {
    return Arrays.stream(annotation.annotationType().getDeclaredMethods())
        .filter(attribute -> !BEAN_NAME_ATTRIBUTES.contains(attribute.getName()))
        .sorted(Comparator.comparing(Method::getName))
        .flatMap(
            attribute -> {
              Object value = AnnotationUtils.getValue(annotation, attribute.getName());
              Object defaultValue =
                  AnnotationUtils.getDefaultValue(annotation, attribute.getName());
              return ObjectUtils.nullSafeEquals(value, defaultValue)
                  ? Stream.empty()
                  : Stream.of("%s=%s".formatted(attribute.getName(), print(value)));
            });
  }

  private static String print(@Nullable Object value) {
    return switch (value) {
      case null -> "null";
      case Class<?> type -> type.getName();
      case Enum<?> constant -> constant.name();
      case Object[] array ->
          Arrays.stream(array).map(BeanOverridePrinter::print).collect(joining(", ", "[", "]"));
      default -> ObjectUtils.nullSafeToString(value);
    };
  }

  private static boolean isBeanOverrideAnnotation(Annotation annotation) {
    return MergedAnnotations.from(annotation.annotationType()).isPresent(BeanOverride.class);
  }
}
