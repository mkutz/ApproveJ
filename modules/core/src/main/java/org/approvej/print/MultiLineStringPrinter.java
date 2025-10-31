package org.approvej.print;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A generic printer for Java {@link Object}s that prints their properties and values one per line.
 */
@NullMarked
public class MultiLineStringPrinter implements Printer<Object> {

  /** A {@link Set} of classes that will be printed directly. */
  public static final Set<Class<?>> SIMPLE_TYPES =
      Set.of(
          Boolean.class,
          Character.class,
          CharSequence.class,
          Class.class,
          Enum.class,
          Number.class,
          UUID.class,
          Temporal.class,
          TemporalAmount.class);

  private static final String PAIR_FORMAT = "%s=%s";

  private Comparator<Field> fieldComparator = (field1, field2) -> 0;

  /**
   * Creates a new {@link MultiLineStringPrinter} instance that prints the given object.
   *
   * <p>This constructor is public to allow instantiation via reflection, e.g. in the {@link
   * org.approvej.Configuration} class.
   */
  public MultiLineStringPrinter() {
    // No initialization needed
  }

  /**
   * Creates a new {@link MultiLineStringPrinter} instance.
   *
   * @return a new {@link MultiLineStringPrinter} instance
   */
  public static MultiLineStringPrinter multiLineString() {
    return new MultiLineStringPrinter();
  }

  /**
   * Creates a new {@link MultiLineStringPrinter} instance.
   *
   * @return a new {@link MultiLineStringPrinter} instance
   * @deprecated use {@link #multiLineString()}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public static MultiLineStringPrinter objectPrinter() {
    return new MultiLineStringPrinter();
  }

  /**
   * Causes the {@link Printer} to sort the printed object's fields by their name. By default, the
   * fields will be printed in the order of their declaration.
   *
   * @return this
   */
  public MultiLineStringPrinter sorted() {
    fieldComparator = Comparator.comparing(Field::getName);
    return this;
  }

  @Override
  public String apply(Object value) {
    return apply(value, "");
  }

  private String apply(@Nullable Object object, String baseIndent) {
    return switch (object) {
      case Map<?, ?> map -> applyMap(map, baseIndent);
      case Collection<?> collection -> applyCollection(collection, baseIndent);
      case null -> applyObject("null", baseIndent);
      default -> applyObject(object, baseIndent);
    };
  }

  private String applyCollection(Collection<?> collection, String baseIndent) {
    if (collection.isEmpty()) {
      return "[]";
    }
    String indent = baseIndent + "  ";
    return collection.stream()
        .map(element -> apply(element, indent))
        .collect(
            joining(
                ",%n%s".formatted(indent),
                "[%n%s".formatted(indent),
                "%n%s]".formatted(baseIndent)));
  }

  private String applyMap(Map<?, ?> map, String baseIndent) {
    return applyCollection(
        new TreeMap<>(map)
            .entrySet().stream()
                .map(
                    entry ->
                        PAIR_FORMAT.formatted(
                            entry.getKey(), apply(entry.getValue(), baseIndent + "  ")))
                .toList(),
        baseIndent);
  }

  private String applyObject(@Nullable Object object, String baseIndent) {
    String indent = baseIndent + "  ";
    if (object == null
        || SIMPLE_TYPES.stream()
            .anyMatch(simpleType -> simpleType.isAssignableFrom(object.getClass()))) {
      return "%s".formatted(object);
    }
    return getFieldsAndGetters(object)
        .filter(FieldAndGetter::hasGetter)
        .map(
            fieldAndGetter ->
                PAIR_FORMAT.formatted(
                    fieldAndGetter.field.getName(), apply(fieldAndGetter.value(), indent)))
        .collect(
            joining(
                ",%n%s".formatted(indent),
                "%s [%n%s".formatted(object.getClass().getSimpleName(), indent),
                "%n%s]".formatted(baseIndent)));
  }

  private Stream<FieldAndGetter> getFieldsAndGetters(Object object) {
    return getFields(object).map(field -> new FieldAndGetter(field, object));
  }

  private Stream<Field> getFields(Object object) {
    return getTypes(object)
        .flatMap(aClass -> Arrays.stream(aClass.getDeclaredFields()))
        .filter(field -> !Modifier.isStatic(field.getModifiers()))
        .sorted(fieldComparator);
  }

  private Stream<Class<?>> getTypes(Object object) {
    return Stream.<Class<?>>iterate(
        object.getClass(), type -> type != Object.class, Class::getSuperclass)
        .toList()
        .reversed()
        .stream();
  }

  private record FieldAndGetter(Object object, Field field, Optional<Method> getter) {

    private FieldAndGetter(Field field, Object object) {
      this(
          object,
          field,
          stream(field.getDeclaringClass().getDeclaredMethods())
              .filter(
                  method ->
                      method.getParameterCount() == 0
                          && Void.class != method.getReturnType()
                          && method
                              .getName()
                              .matches(
                                  String.format(
                                      "(%s|get%2$s|is%2$s)",
                                      field.getName(),
                                      field.getName().substring(0, 1).toUpperCase()
                                          + field.getName().substring(1))))
              .findFirst());
    }

    boolean hasGetter() {
      return getter.isPresent();
    }

    @Nullable Object value() {
      return getter
          .map(
              method -> {
                try {
                  return method.invoke(object);
                } catch (IllegalAccessException | InvocationTargetException e) {
                  return "<inaccessible>";
                }
              })
          .orElse(null);
    }
  }
}
