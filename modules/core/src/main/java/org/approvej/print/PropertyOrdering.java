package org.approvej.print;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility for deterministic property ordering during serialization.
 *
 * <p>Field-backed properties are ordered first in their declaration order, followed by any
 * additional properties (e.g. from getters) in alphabetical order.
 */
public final class PropertyOrdering {

  private PropertyOrdering() {}

  /**
   * Reorders a list of named properties so that field-backed properties appear first in their
   * declaration order, followed by remaining properties sorted alphabetically.
   *
   * @param type the class whose field declaration order determines property ordering
   * @param properties the properties to reorder
   * @param nameExtractor a function to extract the property name from each element
   * @param <T> the type of property elements
   * @return a new list with properties in deterministic order
   */
  public static <T> List<T> reorder(
      Class<?> type, List<T> properties, Function<T, String> nameExtractor) {
    List<String> fieldNames = declaredFieldNames(type);

    Map<String, T> byName = new LinkedHashMap<>();
    for (T prop : properties) {
      byName.put(nameExtractor.apply(prop), prop);
    }

    List<T> ordered = new ArrayList<>(properties.size());
    for (String name : fieldNames) {
      T prop = byName.remove(name);
      if (prop != null) ordered.add(prop);
    }

    byName.values().stream().sorted(Comparator.comparing(nameExtractor)).forEach(ordered::add);

    return ordered;
  }

  private static List<String> declaredFieldNames(Class<?> type) {
    if (type.isRecord()) {
      return Arrays.stream(type.getRecordComponents()).map(RecordComponent::getName).toList();
    }
    List<String> names = new ArrayList<>();
    for (Class<?> clazz = type;
        clazz != null && clazz != Object.class;
        clazz = clazz.getSuperclass()) {
      List<String> current =
          Arrays.stream(clazz.getDeclaredFields())
              .filter(field -> !Modifier.isStatic(field.getModifiers()) && !field.isSynthetic())
              .map(Field::getName)
              .toList();
      names.addAll(0, current);
    }
    return names;
  }
}
