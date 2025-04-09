package org.approvej.print;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;

/**
 * A generic printer for Java {@link Object}s that prints their properties and values one per line.
 *
 * @param <T> the type of the object to be printed
 */
@NullMarked
public class ObjectPrinter<T> implements Printer<T> {

  /** A {@link Set} of methods that won't be regarded as property getters. */
  public static final Set<String> EXCLUDED_METHODS = Set.of("hashCode", "toString", "hash");

  /** A {@link Set} of classes that will be printed directly. */
  public static final Set<Class<?>> SIMPLE_TYPES =
      Set.of(
          Boolean.class,
          CharSequence.class,
          Number.class,
          UUID.class,
          Temporal.class,
          TemporalAmount.class);

  private static final String PAIR_FORMAT = "%s=%s";

  /**
   * Creates a new {@link ObjectPrinter} instance.
   *
   * @return a new {@link ObjectPrinter} instance
   * @param <T> the type of the object to be printed
   */
  public static <T> ObjectPrinter<T> objectPrinter() {
    return new ObjectPrinter<>();
  }

  private ObjectPrinter() {}

  @Override
  public String apply(Object value) {
    return apply(value, "");
  }

  private String apply(Object object, String baseIndent) {
    return switch (object) {
      case Map<?, ?> map -> applyMap(map, baseIndent);
      case Collection<?> collection -> applyCollection(collection, baseIndent);
      default -> applyObject(object, baseIndent);
    };
  }

  private String applyCollection(Collection<?> collection, String baseIndent) {
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

  private String applyObject(Object object, String baseIndent) {
    String indent = baseIndent + "  ";
    Class<?> type = object.getClass();
    if (SIMPLE_TYPES.stream().anyMatch(simpleType -> simpleType.isAssignableFrom(type))) {
      return object.toString();
    }
    return stream(object.getClass().getDeclaredMethods())
        .filter(method -> !EXCLUDED_METHODS.contains(method.getName()))
        .filter(method -> method.getParameterCount() == 0)
        .filter(method -> Void.class != method.getReturnType())
        .sorted(Comparator.comparing(Method::getName))
        .map(
            method -> {
              try {
                return PAIR_FORMAT.formatted(
                    method.getName(), apply(method.invoke(object), indent));
              } catch (IllegalAccessException | InvocationTargetException e) {
                return PAIR_FORMAT.formatted(method.getName(), "<inaccessible>");
              }
            })
        .collect(
            joining(
                ",%n%s".formatted(indent),
                "%s [%n%s".formatted(object.getClass().getSimpleName(), indent),
                "%n%s]".formatted(baseIndent)));
  }
}
