package org.approvej.scrub;

/**
 * A builder to configure a {@link Scrubber}.
 *
 * @param <T> the type of the value to be scrubbed
 */
public interface ScrubberBuilder<T> extends Scrubber<T> {

  /**
   * Builds the {@link Scrubber} with the current configuration.
   *
   * @return a new {@link Scrubber} with the current configuration
   */
  Scrubber<T> build();

  /**
   * Builds the {@link Scrubber} with the current configuration and applies it to the given value.
   *
   * @param value the value to be scrubbed
   * @return the scrubbed value
   */
  @Override
  default T apply(T value) {
    return build().apply(value);
  }
}
