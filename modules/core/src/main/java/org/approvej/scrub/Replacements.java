package org.approvej.scrub;

import java.util.function.Function;

/**
 * Collection of replacement functions ({@link Function}s that take an {@link Integer} and return an
 * {@link Object}) for use with {@link Scrubber}s.
 */
public class Replacements {

  private Replacements() {
    // Utility class
  }

  /**
   * Replaces with "<code>[name #]</code>" where <code>name</code> is the given name and '#' is the
   * number of the distinct replacement.
   *
   * @param name a String used to identify the replacement
   * @return a replacement function that replaces with "[name #]"
   */
  public static Function<Integer, Object> numbered(String name) {
    return number -> String.format("[%s %d]", name, number);
  }

  /**
   * Replaces each match with "[scrubbed #]" where '#' is the number of the distinct replacement.
   *
   * @return a replacement function that replaces with "[scrubbed #]"
   */
  public static Function<Integer, Object> numbered() {
    return number -> String.format("[scrubbed %d]", number);
  }

  /**
   * Replaces each match with the given static replacement string.
   *
   * @param replacement the static replacement string
   * @return a replacement function that always returns the same string
   */
  public static Function<Integer, Object> string(String replacement) {
    return number -> replacement;
  }
}
