package org.approvej.scrub;

import java.time.format.DateTimeFormatter;
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
  public static Replacement numbered(String label) {
    return (match, count) -> String.format("[%s %d]", label, count);
  }

  /**
   * Replaces each match with "[scrubbed #]" where '#' is the number of the distinct replacement.
   *
   * @return a replacement function that replaces with "[scrubbed #]"
   */
  public static Replacement numbered() {
    return numbered("scrubbed");
  }

  /**
   * Replaces each match with the given static replacement string.
   *
   * @param replacement the static replacement string
   * @return a replacement function that always returns the same string
   */
  public static Replacement string(String replacement) {
    return (match, count) -> replacement;
  }

  /**
   * Replaces each match of the given dateTimePattern (as defined by {@link DateTimeFormatter}) with
   * a relative description, like {@code [today]}, {@code [yesterday]}, {@code [13 days from now]}.
   *
   * @param dateTimeFormatter the {@link DateTimeFormatter} to parse the date/time strings
   * @return a replacement function that returns a relative description for dates of the given
   *     dateTimePattern
   */
  public static Replacement relativeDate(DateTimeFormatter dateTimeFormatter) {
    return new RelativeDateReplacement(dateTimeFormatter);
  }

  /**
   * Replaces each match of the given dateTimePattern (as defined by {@link DateTimeFormatter}) with
   * a relative description, like {@code [today]}, {@code [yesterday]}, {@code [13 days from now]}.
   *
   * @param dateTimePattern a pattern as defined by {@link DateTimeFormatter}
   * @return a replacement function that returns a relative description for dates of the given
   *     dateTimePattern
   */
  public static Replacement relativeDate(String dateTimePattern) {
    return relativeDate(DateTimeFormatter.ofPattern(dateTimePattern));
  }

  /**
   * Masks each letter or digit of the match with a generic one. E.g. all latin uppercase letters
   * are replaced with {@code A}, hence the String "John Doe" is replaced with "Aaaa Aaa".
   *
   * <p>This {@link Replacement} is generally useful for well-structured strings that do not vary in
   * length or composition like order numbers, IDs, or strict date/time strings. It is not a good
   * choice for names as they usually vary in length, or UUIDs as they are composed of random
   * characters and digits (hexadecimal).
   */
  public static Replacement masking() {
    return (match, count) ->
        match
            .replaceAll("\\p{M}", "")
            .replaceAll("\\p{Lu}", "A")
            .replaceAll("(\\p{Ll}|\\p{Lt}|\\p{Lm}|\\p{Lo})", "a")
            .replaceAll("\\p{N}", "1");
  }
}
