package org.approvej.scrub;

import java.lang.reflect.Field;
import org.jspecify.annotations.NullMarked;

@NullMarked
record FieldScrubberRecord<T>(Field field, Replacement<Object> replacement)
    implements FieldScrubber<T> {

  @Override
  public FieldScrubber<T> replacement(Replacement<Object> replacement) {
    return new FieldScrubberRecord<>(field, replacement);
  }

  @Override
  public T apply(T value) {
    try {
      field.setAccessible(true); // NOSONAR
      field.set(value, replacement.apply(field.get(value), 1)); // NOSONAR
    } catch (IllegalAccessException e) {
      throw new ScrubbingError(
          "Failed to scrub field %s on value %s".formatted(field.getName(), value), e);
    }
    return value;
  }
}
