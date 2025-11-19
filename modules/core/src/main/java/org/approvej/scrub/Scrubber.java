package org.approvej.scrub;

import java.util.function.UnaryOperator;

/**
 * An {@link UnaryOperator} that scrubs certain information from a value. This might be useful
 * especially for dynamic data like timestamps, dates or generally random values.
 *
 * @param <I> the type of the scrubber itself
 * @param <T> the type of value to scrub
 * @param <R> the type of the replacement
 */
public interface Scrubber<I extends Scrubber<I, T, R>, T, R> extends UnaryOperator<T> {

  /**
   * Creates a copy of this using the given {@link Replacement}.
   *
   * @param replacement a {@link Replacement} function
   * @return a copy of this using the given {@link Replacement}
   */
  I replacement(Replacement<R> replacement);

  /**
   * Creates a copy of this using the given staticReplacement.
   *
   * @param staticReplacement a static value that will replace any match of the scrubber
   * @return a copy of this using the given staticReplacement
   */
  default I replacement(R staticReplacement) {
    return replacement(((match, count) -> staticReplacement));
  }
}
