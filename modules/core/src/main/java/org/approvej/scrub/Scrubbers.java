package org.approvej.scrub;

import static org.approvej.scrub.Replacements.numbered;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;
import org.jspecify.annotations.NullMarked;

/** Collection of static methods to create {@link Scrubber} instances. */
@NullMarked
public class Scrubbers {

  private Scrubbers() {}

  private static final Pattern UUID_PATTERN =
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
   * Creates {@link DateTimeScrubber} to replace date/time strings of the given pattern localized by
   * the given locale.
   *
   * @param dateTimePattern a pattern as defined by {@link DateTimeFormatter}
   * @param locale the {@link Locale} to localize the date/time pattern (influences names of months
   *     or weekdays for example)
   * @return a {@link DateTimeScrubber} for the given date/time pattern
   * @see DateTimeFormatter
   */
  public static RegexScrubber dateTimeFormat(String dateTimePattern, Locale locale) {
    return new DateTimeScrubber(dateTimePattern, locale, numbered("datetime"));
  }

  /**
   * Creates {@link DateTimeScrubber} to replace date/time strings of the given pattern localized by
   * the default {@link Locale}.
   *
   * @param dateTimePattern a pattern as defined by {@link DateTimeFormatter}
   * @return a {@link DateTimeScrubber} for the given date/time pattern
   * @see DateTimeFormatter
   * @see Locale#getDefault()
   */
  public static RegexScrubber dateTimeFormat(String dateTimePattern) {
    return dateTimeFormat(dateTimePattern, Locale.getDefault());
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
