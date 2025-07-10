package org.approvej.scrub;

import java.util.function.BiFunction;

/** A {@link BiFunction}, that defines how a match in the {@link RegexScrubber} is replaced. */
public interface Replacement extends BiFunction<String, Integer, String> {

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
  String apply(String match, Integer count);
}
