package org.approvej.scrub;

import java.lang.reflect.Field;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Generic {@link Scrubber} to set the value of a certain field via reflection.
 *
 * <p>Note that this requires that the field is generally mutable. Immutable fields that will cause
 * a {@link ScrubbingError} when this is {@link #apply(Object) applied}.
 *
 * @param <T> the type of value to scrub
 */
@NullMarked
public class FieldScrubber<T> implements Scrubber<T> {

  private final Field field;
  private final @Nullable Object replacement;

  /**
   * Creates a new {@link FieldScrubber} for a field's value to be replaced with the given
   * replacement.
   *
   * @param field the {@link Field} to be scrubbed
   * @param replacement the {@link Object} to replace the field's value with
   */
  public FieldScrubber(Field field, @Nullable Object replacement) {
    this.field = field;
    this.replacement = replacement;
  }

  /**
   * Scrubs the given value.
   *
   * @param value the object to be scrubbed
   * @return the scrubbed object
   * @throws ScrubbingError if the field cannot be edited
   */
  @Override
  public T apply(T value) {
    try {
      field.setAccessible(true); // NOSONAR this accessibility update is needed for the use case
      field.set(value, replacement); // NOSONAR this accessibility bypass is needed for the use case
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
    return new FieldScrubber<>(field, replacement);
  }
}
