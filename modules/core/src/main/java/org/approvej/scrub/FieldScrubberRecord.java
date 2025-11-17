package org.approvej.scrub;

import java.lang.reflect.Field;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
record FieldScrubberRecord<T>(Field field, @Nullable Object replacement)
    implements FieldScrubber<T> {

  @Override
  public FieldScrubber<T> replacement(Object replacement) {
    return new org.approvej.scrub.FieldScrubberRecord<>(field, replacement);
  }

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
}
