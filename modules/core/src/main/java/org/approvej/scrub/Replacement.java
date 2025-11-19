package org.approvej.scrub;

import java.util.function.BiFunction;
import org.jspecify.annotations.Nullable;

/**
 * A {@link BiFunction}, that defines how a match is replaced.
 *
 * @param <R> the type of the replaced value
 */
public interface Replacement<R> extends BiFunction<R, Integer, R> {

  /**
   * Will return a replacement for the given match and count.
   *
   * <p>The count will be the same for equal matches. E.g. if all vowels ({@code [aeiou]}) should be
   * replaced in the text {@code Hello there}, this will be applied four times:
   *
   * <ol>
   *   <li>{@code He}<br>
   *       {@code apply("e", 1)},
   *   <li>{@code Hello}<br>
   *       {@code apply("o", 2)},
   *   <li>{@code Hello the}<br>
   *       {@code apply("e", 1)},
   *   <li>{@code Hello there}<br>
   *       {@code apply("e", 1)}
   * </ol>
   *
   * @param match the found string that will be replaced
   * @param count the number of the distinct match
   * @return the replacement
   */
  @Override
  @Nullable R apply(R match, Integer count);
}
