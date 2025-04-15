package org.approvej.scrub;

/**
 * A builder to configure a {@link Scrubber}.
 *
 * @param <T> the type of the value to be scrubbed
 */
public interface ScrubberBuilder<T> extends Scrubber<T> {

  Scrubber<T> build();

  @Override
  default T apply(T value) {
    return build().apply(value);
  }
}
