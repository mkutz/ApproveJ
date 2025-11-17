package org.approvej.scrub;

import java.time.Duration;
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
   * Replaces with "{@code [label #]}" where {@code label} is the given label and {@code #} is the
   * number of the distinct replacement.
   *
   * @param label a String used to identify the replacement
   * @return a replacement function that replaces with "{@code [label #]}"
   */
  public static Replacement numbered(String label) {
    return (match, count) -> String.format("[%s %d]", label, count);
  }

  /**
   * Replaces each match with "{@code [scrubbed #]}" where {@code #} is the number of the distinct
   * replacement.
   *
   * @return a replacement function that replaces with "{@code [scrubbed #]}"
   */
  public static Replacement numbered() {
    return numbered("scrubbed");
  }

  /**
   * Replaces with "{@code [label]}" where "{@code label}" is the given label.
   *
   * @param label a String used to identify the replacement
   * @return a replacement function that replaces with "{@code [label]}"
   */
  public static Replacement labeled(String label) {
    return (match, count) -> String.format("[%s]", label);
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
   * Replaces each match of the {@link DateTimeScrubber} with a relative duration, like {@code
   * [today]}, {@code [yesterday]}, {@code [in 13 days]}, {@code [1 year 20 days ago]}.
   *
   * @return a new {@link RelativeDateReplacement}
   */
  public static RelativeDateReplacement relativeDate() {
    return new RelativeDateReplacementRecord(DateTimeFormatter.ISO_DATE);
  }

  /**
   * Replaces each match of the {@link DateTimeScrubber} with a relative duration, like {@code
   * [now]}, {@code [in 1d 23h 59m 59s]}, {@code [10s ago]}.
   *
   * @return a new {@link RelativeDateReplacement}
   */
  public static RelativeDateTimeReplacement relativeDateTime() {
    return new RelativeDateTimeReplacementRecord(
        DateTimeFormatter.ISO_DATE_TIME, Duration.ofSeconds(1));
  }

  /**
   * Masks each letter or digit of the match with a generic one. E.g. all latin uppercase letters
   * are replaced with {@code A}, hence the String "John Doe" is replaced with "Aaaa Aaa".
   *
   * <p>This {@link Replacement} is generally useful for well-structured strings that do not vary in
   * length or composition like order numbers, IDs, or strict date/time strings. It is not a good
   * choice for names as they usually vary in length, or UUIDs as they are composed of random
   * characters and digits (hexadecimal).
   *
   * @return a replacement function replaces each character with a generic one
   */
  public static Replacement masking() {
    return (match, count) ->
        match
            .replaceAll("\\p{M}", "")
            .replaceAll("\\p{Lu}", "A")
            .replaceAll("[\\p{Ll}\\p{Lt}\\p{Lm}\\p{Lo}]", "a")
            .replaceAll("\\p{N}", "1");
  }
}
