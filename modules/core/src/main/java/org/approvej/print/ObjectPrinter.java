package org.approvej.print;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A generic printer for Java {@link Object}s that prints their properties and values one per line.
 *
 * @param <T> the type of the object to be printed
 */
@NullMarked
public class ObjectPrinter<T> implements Printer<T> {

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
   * Creates a new {@link ObjectPrinter} instance that prints the given object.
   *
   * <p>This constructor is public to allow instantiation via reflection, e.g. in the {@link
   * org.approvej.Configuration} class.
   */
  public ObjectPrinter() {
    // No initialization needed
  }

  /**
   * Creates a new {@link ObjectPrinter} instance.
   *
   * @return a new {@link ObjectPrinter} instance
   * @param <T> the type of the object to be printed
   */
  public static <T> ObjectPrinter<T> objectPrinter() {
    return new ObjectPrinter<>();
  }

  /**
   * Causes the {@link Printer} to sort the printed object's fields by their name. By default, the
   * fields will be printed in the order of their declaration.
   *
   * @return this
   */
  public ObjectPrinter<T> sorted() {
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
    return stream(object.getClass().getDeclaredFields())
        .filter(field -> !Modifier.isStatic(field.getModifiers()))
        .sorted(fieldComparator)
        .map(
            field -> PAIR_FORMAT.formatted(field.getName(), apply(getValue(object, field), indent)))
        .collect(
            joining(
                ",%n%s".formatted(indent),
                "%s [%n%s".formatted(object.getClass().getSimpleName(), indent),
                "%n%s]".formatted(baseIndent)));
  }

  @Nullable
  private Object getValue(Object object, Field field) {
    String fieldName = field.getName();
    String capitalized = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    String methodNameRegex = String.format("(%s|get%s|is%s)", fieldName, capitalized, capitalized);
    return stream(object.getClass().getDeclaredMethods())
        .filter(method -> method.getParameterCount() == 0)
        .filter(method -> Void.class != method.getReturnType())
        .filter(method -> method.getName().matches(methodNameRegex))
        .findFirst()
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
