package org.approvej.scrub;

import static org.approvej.scrub.Replacements.numbered;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import org.jspecify.annotations.NullMarked;

/** Collection of static methods to create {@link Scrubber} instances. */
@NullMarked
public class Scrubbers {

  private Scrubbers() {}

  public static final Pattern UUID_PATTERN =
      Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

  /**
   * Creates a {@link RegexScrubber} with the given pattern.
   *
   * @param pattern the {@link Pattern} matching the strings to be scrubbed
   * @return a {@link RegexScrubber} with the given pattern
   */
  public static RegexScrubber stringsMatching(Pattern pattern) {
    return new RegexScrubber(pattern, numbered());
  }

  /**
   * Creates a {@link RegexScrubber} with the given pattern.
   *
   * @param pattern the pattern matching the string to be scrubbed as {@link String}
   * @return a {@link RegexScrubber} with the given pattern
   * @see Pattern#compile(String)
   */
  public static RegexScrubber stringsMatching(String pattern) {
    return stringsMatching(Pattern.compile(pattern));
  }

  /**
   * Creates {@link DateTimeScrubber} to replace date/time strings of the given pattern.
   *
   * @param dateTimePattern a pattern as defined by {@link DateTimeFormatter}
   * @return a {@link DateTimeScrubber} for the given date/time pattern
   * @see DateTimeFormatter
   */
  public static RegexScrubber dateTimeFormat(String dateTimePattern) {
    return new DateTimeScrubber(dateTimePattern, numbered("datetime"));
  }

  private static final LocalDate EXAMPLE_DATE = LocalDate.of(4567, 12, 30);

  /**
   * Creates {@link RegexScrubber} to replace date strings of the given pattern.
   *
   * @param formatter a {@link DateTimeFormatter} to parse the dates
   * @return a new {@link RegexScrubber} with the given {@link DateTimeFormatter} turned into a
   *     {@link Pattern}
   * @deprecated use {@link #dateTimeFormat(String)} instead
   */
  @Deprecated(since = "0.6.1", forRemoval = true)
  public static RegexScrubber dates(DateTimeFormatter formatter) {
    return stringsMatching(
            Pattern.compile(
                formatter
                    .format(EXAMPLE_DATE)
                    .replaceAll("\\p{L}+", "\\\\p{L}+")
                    .replaceAll("\\d", "\\\\d")))
        .replacement(numbered("date"));
  }

  private static final ZonedDateTime EXAMPLE_INSTANT = ZonedDateTime.now();

  /**
   * {@link RegexScrubber} that replaces instant strings of the given pattern.
   *
   * @param dateFormatPattern a {@link DateTimeFormatter} to parse the dates
   * @return a new {@link RegexScrubber} with the given {@link DateTimeFormatter} turned into a
   *     {@link Pattern}
   * @deprecated use {@link #dateTimeFormat(String)}
   */
  @Deprecated(since = "0.6.1", forRemoval = true)
  public static RegexScrubber instants(DateTimeFormatter dateFormatPattern) {
    return stringsMatching(
            Pattern.compile(
                dateFormatPattern
                    .format(EXAMPLE_INSTANT)
                    .replaceAll("\\p{L}+", "\\\\p{L}+")
                    .replaceAll("\\d", "\\\\d")))
        .replacement(numbered("instant"));
  }

  /**
   * {@link RegexScrubber} for UUIDs.
   *
   * @return a {@link RegexScrubber} that replaces all UUIDs
   */
  public static RegexScrubber uuids() {
    return stringsMatching(UUID_PATTERN).replacement(numbered("uuid"));
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
