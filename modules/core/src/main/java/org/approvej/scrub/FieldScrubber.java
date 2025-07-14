package org.approvej.scrub;

import java.lang.reflect.Field;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Generic {@link Scrubber} to set the value of a certain field via reflection.
 *
 * <p>Note that this requires that the field is generally mutable. Fields that are declared final
 * will cause a {@link ScrubbingError} when this is {@link #apply(Object) applied}.
 *
 * @param <T> the type of value to scrub
 */
@NullMarked
public class FieldScrubber<T> implements Scrubber<T> {

  private final Field field;
  @Nullable private Object replacement;

  /**
   * Creates a new {@link FieldScrubber} for a field's value to be replaced with null.
   *
   * @param field the field to be scrubbed
   */
  FieldScrubber(Field field) {
    this.field = field;
  }

  /**
   * Creates a new {@link FieldScrubber} for a field's value to be replaced with null.
   *
   * @param type the {@link Class} which should be scrubbed
   * @param fieldName the name of the field to be scrubbed
   */
  FieldScrubber(Class<T> type, String fieldName) {
    try {
      this.field = type.getField(fieldName);
    } catch (NoSuchFieldException e) {
      throw new ScrubbingError(
          "Cannot create FieldScrubber for field %s on class %s".formatted(fieldName, type), e);
    }
  }

  /**
   * Scrubs the given value.
   *
   * @param value the object to be scrubbed
   * @return the scrubbed object
   * @throws ScrubbingError if the field cannot the edited
   */
  @Override
  public T apply(T value) {
    try {
      field.setAccessible(true);
      field.set(value, replacement);
    } catch (IllegalAccessException e) {
      throw new ScrubbingError(
          "Failed to scrub field %s on value %s".formatted(field.getName(), value), e);
    }
    return value;
  }

  /**
   * Sets the replacement {@link Object}.
   *
   * @param replacement the new replacement {@link Object}
   * @return this
   */
  public FieldScrubber<T> replacement(Object replacement) {
    this.replacement = replacement;
    return this;
  }
}
