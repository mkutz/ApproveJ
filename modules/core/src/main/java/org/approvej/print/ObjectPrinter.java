package org.approvej.print;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Stream;
import org.approvej.Configuration;
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
  private static final Set<Class<?>> SIMPLE_TYPES =
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

  private final Comparator<Field> fieldComparator;

  /**
   * @param fieldComparator a {@link Comparator} used to sort the printed value's {@link Field}s
   */
  ObjectPrinter(Comparator<Field> fieldComparator) {
    this.fieldComparator = fieldComparator;
  }

  /**
   * Creates a new {@link ObjectPrinter} instance that prints the given object.
   *
   * <p>This constructor is public to allow instantiation via reflection, e.g. in the {@link
   * Configuration} class.
   */
  ObjectPrinter() {
    this((field1, field2) -> 0);
  }

  /**
   * Creates a new {@link ObjectPrinter} instance.
   *
   * @param <T> the type of the object to be printed
   * @return a new {@link ObjectPrinter} instance
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
    return new ObjectPrinter<>(Comparator.comparing(Field::getName));
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
        .flatMap(aClass -> stream(aClass.getDeclaredFields()))
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
