package org.approvej.scrub;

import java.util.function.Function;
import org.jspecify.annotations.NullMarked;

/**
 * Collection of replacement functions ({@link Function}s that take an {@link Integer} and return an
 * {@link Object}) for use with {@link Scrubber}s.
 */
@NullMarked
public class Replacements {

  private Replacements() {}

  /**
   * Replaces with "<code>[label #]</code>" where <code>label</code> is the given label and '#' is
   * the number of the distinct replacement.
   *
   * @param label a String used to identify the replacement
   * @return a replacement function that replaces with "[label #]"
   */
  public static Function<Integer, Object> numbered(String label) {
    return number -> String.format("[%s %d]", label, number);
  }

  /**
   * Replaces each match with "[scrubbed #]" where '#' is the number of the distinct replacement.
   *
   * @return a replacement function that replaces with "[scrubbed #]"
   */
  public static Function<Integer, Object> numbered() {
    return numbered("scrubbed");
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
