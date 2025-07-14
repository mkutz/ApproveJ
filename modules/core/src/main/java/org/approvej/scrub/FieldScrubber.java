package org.approvej.scrub;

import java.lang.reflect.Field;

/**
 * Generic {@link Scrubber} to set the value of a certain field via reflection.
 *
 * <p>Note that this requires that the field is generally mutable. Fields that are declared final
 * will cause a {@link ScrubbingError}
 *
 * @param <T> the type of value to scrub
 */
public class FieldScrubber<T> implements Scrubber<T> {

  private final Field field;
  private final Object replacement;

  /**
   * Creates a new {@link FieldScrubber} for the given field's value to be replaced with the given
   * replacement {@link Object}.
   *
   * @param field the field to be scrubbed
   * @param replacement the replacement object
   */
  FieldScrubber(Field field, Object replacement) {
    this.field = field;
    this.replacement = replacement;
  }

  @Override
  public T apply(T value) {
    if (field.trySetAccessible()) {
      try {
        field.set(value, replacement);
      } catch (IllegalAccessException e) {
        throw new ScrubbingError(
            "Failed to scrub field %s on value %s".formatted(field.getName(), value), e);
      }
    }
    return value;
  }
}
