package org.approvej.scrub;

import java.lang.reflect.Field;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Generic {@link Scrubber} to set the value of a certain field via reflection.
 *
 * <p>Note that this requires that the field is generally mutable. Immutable fields that
 * will cause a {@link ScrubbingError} when this is {@link #apply(Object) applied}.
 *
 * @param field the {@link Field} to be scrubbed
 * @param <T> the type of value to scrub
 */
@NullMarked
public record FieldScrubber<T>(
    Field field, @Nullable Object replacement
) implements Scrubber<T> {

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
