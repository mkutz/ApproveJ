package org.approvej.scrub;

import static org.approvej.scrub.Replacements.numbered;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import org.approvej.scrub.RegexScrubber.RegexScrubberBuilder;
import org.jspecify.annotations.NullMarked;

/**
 * Collection of static methods to create {@link Scrubber} and {@link ScrubberBuilder} instances.
 */
@NullMarked
public class Scrubbers {

  private Scrubbers() {}

  private static final LocalDate EXAMPLE_DATE = LocalDate.of(4567, 12, 30);

  /**
   * Creates a {@link RegexScrubberBuilder} with the given pattern.
   *
   * @param pattern the {@link Pattern} matching the strings to be scrubbed
   * @return a {@link RegexScrubberBuilder} with the given pattern
   */
  public static RegexScrubberBuilder stringsMatching(Pattern pattern) {
    return new RegexScrubberBuilder(pattern);
  }

  /**
   * Creates a {@link RegexScrubberBuilder} with the given pattern.
   *
   * @param pattern the pattern matching the string to be scrubbed as {@link String}
   * @return a {@link RegexScrubberBuilder} with the given pattern
   * @see Pattern#compile(String)
   */
  public static RegexScrubberBuilder stringsMatching(String pattern) {
    return stringsMatching(Pattern.compile(pattern));
  }

  /**
   * Creates {@link RegexScrubberBuilder} to replace date strings of the given pattern.
   *
   * @param dateFormatPattern a {@link DateTimeFormatter} to parse the dates
   * @return a new {@link RegexScrubberBuilder} with the given {@link DateTimeFormatter} turned into
   *     a {@link Pattern}
   */
  public static RegexScrubberBuilder dates(DateTimeFormatter dateFormatPattern) {
    return stringsMatching(
            Pattern.compile(
                dateFormatPattern
                    .format(EXAMPLE_DATE)
                    .replaceAll("\\p{L}+", "\\\\p{L}+")
                    .replaceAll("\\d", "\\\\d")))
        .replacement(numbered("date"));
  }

  private static final ZonedDateTime EXAMPLE_INSTANT = ZonedDateTime.now();

  /**
   * {@link RegexScrubberBuilder} that replaces instant strings of the given pattern.
   *
   * @param dateFormatPattern a {@link DateTimeFormatter} to parse the dates
   * @return a new {@link RegexScrubberBuilder} with the given {@link DateTimeFormatter} turned into
   *     a {@link Pattern}
   */
  public static RegexScrubberBuilder instants(DateTimeFormatter dateFormatPattern) {
    return stringsMatching(
            Pattern.compile(
                dateFormatPattern
                    .format(EXAMPLE_INSTANT)
                    .replaceAll("\\p{L}+", "\\\\p{L}+")
                    .replaceAll("\\d", "\\\\d")))
        .replacement(numbered("instant"));
  }

  /**
   * {@link RegexScrubberBuilder} for UUIDs.
   *
   * @return a {@link RegexScrubber} that replaces all UUIDs
   */
  public static RegexScrubberBuilder uuids() {
    return stringsMatching(
            Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
        .replacement(numbered("uuid"));
  }

  /**
   * Creates a {@link Scrubber} to replace date strings of the given pattern.
   *
   * @param dateFormatPattern a {@link DateTimeFormatter} to parse the dates
   * @return a new {@link RelativeDateScrubber} with the given {@link DateTimeFormatter}.
   */
  public static RelativeDateScrubber relativeDates(DateTimeFormatter dateFormatPattern) {
    return new RelativeDateScrubber(dateFormatPattern);
  }
}
