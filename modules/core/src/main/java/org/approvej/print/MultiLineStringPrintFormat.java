package org.approvej.print;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
 *
 * @param printer the printer implementing this format
 */
@NullMarked
public record MultiLineStringPrintFormat(Printer<Object> printer)
    implements PrintFormat<Object>, PrintFormatProvider<Object> {

  /** Default constructor using a {@link MultiLineStringPrinter} with no sorting. */
  public MultiLineStringPrintFormat() {
    this(new MultiLineStringPrinter<>(null));
  }

  /**
   * Sort the printed object's properties alphabetically by their name. By default, field-backed
   * properties are printed in declaration order, followed by getter-only properties alphabetically.
   *
   * @return a copy of this sorting all properties by name
   */
  public MultiLineStringPrintFormat sorted() {
    return new MultiLineStringPrintFormat(new MultiLineStringPrinter<>(Comparator.naturalOrder()));
  }

  @Override
  public String alias() {
    return "multiLineString";
  }

  @Override
  public PrintFormat<Object> create() {
    return new MultiLineStringPrintFormat();
  }

  /**
   * Creates a new {@link MultiLineStringPrintFormat}.
   *
   * @return a new {@link MultiLineStringPrintFormat}
   */
  public static MultiLineStringPrintFormat multiLineString() {
    return new MultiLineStringPrintFormat();
  }

  /**
   * A generic printer for Java {@link Object}s that prints their properties and values one per
   * line.
   *
   * @param propertyNameComparator when non-null, all properties are sorted by name using this
   *     comparator; when null, field-backed properties appear in declaration order followed by
   *     getter-only properties alphabetically
   * @param <T> the type of the object to be printed
   */
  @NullMarked
  private record MultiLineStringPrinter<T>(@Nullable Comparator<String> propertyNameComparator)
      implements Printer<T> {

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
      List<Property> properties = discoverProperties(object);
      return properties.stream()
          .map(property -> PAIR_FORMAT.formatted(property.name(), apply(property.value(), indent)))
          .collect(
              joining(
                  ",%n%s".formatted(indent),
                  "%s [%n%s".formatted(object.getClass().getSimpleName(), indent),
                  "%n%s]".formatted(baseIndent)));
    }

    private List<Property> discoverProperties(Object object) {
      List<Class<?>> hierarchy =
          Stream.<Class<?>>iterate(
                  object.getClass(), type -> type != Object.class, Class::getSuperclass)
              .toList()
              .reversed();

      Set<String> allFieldNames =
          hierarchy.stream()
              .flatMap(clazz -> stream(clazz.getDeclaredFields()))
              .filter(field -> !Modifier.isStatic(field.getModifiers()) && !field.isSynthetic())
              .map(Field::getName)
              .collect(toSet());

      Set<String> seen = new HashSet<>();
      List<Property> properties =
          hierarchy.stream()
              .flatMap(
                  clazz ->
                      Stream.concat(
                          fieldBackedProperties(clazz, object, seen),
                          getterOnlyProperties(clazz, object, allFieldNames, seen)))
              .toList();

      if (propertyNameComparator != null) {
        return properties.stream()
            .sorted(Comparator.comparing(Property::name, propertyNameComparator))
            .toList();
      }

      return PropertyOrdering.reorder(object.getClass(), properties, Property::name);
    }

    private Stream<Property> fieldBackedProperties(
        Class<?> clazz, Object object, Set<String> seen) {
      return stream(clazz.getDeclaredFields())
          .filter(field -> !Modifier.isStatic(field.getModifiers()) && !field.isSynthetic())
          .flatMap(
              field ->
                  findGetterForField(field, clazz)
                      .filter(getter -> seen.add(field.getName()))
                      .map(getter -> new Property(field.getName(), object, getter))
                      .stream());
    }

    private Stream<Property> getterOnlyProperties(
        Class<?> clazz, Object object, Set<String> allFieldNames, Set<String> seen) {
      return stream(clazz.getDeclaredMethods())
          .filter(
              method ->
                  !Modifier.isStatic(method.getModifiers())
                      && !method.isSynthetic()
                      && method.getParameterCount() == 0
                      && method.getReturnType() != void.class)
          .flatMap(
              method ->
                  derivePropertyName(method)
                      .filter(name -> !allFieldNames.contains(name))
                      .filter(name -> seen.add(name))
                      .map(name -> new Property(name, object, method))
                      .stream());
    }

    private Optional<Method> findGetterForField(Field field, Class<?> clazz) {
      String fieldName = field.getName();
      String capitalized = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
      Set<String> candidates = Set.of(fieldName, "get" + capitalized, "is" + capitalized);
      return stream(clazz.getDeclaredMethods())
          .filter(
              method ->
                  !Modifier.isStatic(method.getModifiers())
                      && !method.isSynthetic()
                      && method.getParameterCount() == 0
                      && method.getReturnType() != void.class)
          .filter(method -> candidates.contains(method.getName()))
          .filter(method -> !method.getName().startsWith("is") || isBooleanReturnType(method))
          .findFirst();
    }

    private Optional<String> derivePropertyName(Method method) {
      String name = method.getName();
      if (name.startsWith("get") && name.length() > 3 && Character.isUpperCase(name.charAt(3))) {
        return Optional.of(Character.toLowerCase(name.charAt(3)) + name.substring(4));
      }
      if (name.startsWith("is")
          && name.length() > 2
          && Character.isUpperCase(name.charAt(2))
          && isBooleanReturnType(method)) {
        return Optional.of(Character.toLowerCase(name.charAt(2)) + name.substring(3));
      }
      return Optional.empty();
    }

    private static boolean isBooleanReturnType(Method method) {
      Class<?> returnType = method.getReturnType();
      return returnType == boolean.class || returnType == Boolean.class;
    }

    private record Property(String name, Object target, Method accessor) {

      @Nullable Object value() {
        try {
          return accessor.invoke(target);
        } catch (IllegalAccessException | InvocationTargetException e) {
          return "<inaccessible>";
        }
      }
    }
  }
}
