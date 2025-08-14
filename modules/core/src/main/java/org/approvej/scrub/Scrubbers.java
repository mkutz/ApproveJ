package org.approvej.scrub;

import static java.util.stream.Collectors.joining;
import static org.approvej.scrub.Replacements.numbered;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;

/** Collection of static methods to create {@link Scrubber} instances. */
@NullMarked
public class Scrubbers {

  private Scrubbers() {}

  private static final Pattern UUID_PATTERN =
      Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

  /**
   * Creates a {@link RegexScrubber} for all given strings.
   *
   * <p>This type of {@link Scrubber} is particularly useful if dynamic parts of the value are
   * known. E.g. if they were part of the import parameters of the method under test.
   *
   * @param first the first {@link String} that should be scrubbed
   * @param more more {@link String}s that should be scrubbed
   * @return a {@link RegexScrubber} for all given strings
   */
  public static RegexScrubber strings(String first, String... more) {
    return new RegexScrubber(
        Pattern.compile(
            Stream.concat(Stream.of(first), Arrays.stream(more))
                .map(Pattern::quote)
                .collect(joining("|", "(", ")"))),
        numbered());
  }

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
  public static DateTimeScrubber dateTimeFormat(String dateTimePattern, Locale locale) {
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
  public static DateTimeScrubber dateTimeFormat(String dateTimePattern) {
    return dateTimeFormat(dateTimePattern, Locale.getDefault());
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 local dates, like {@code 2019-02-25}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 local dates
   * @see DateTimeFormatter#ISO_LOCAL_DATE
   */
  public static DateTimeScrubber isoLocalDates() {
    return dateTimeFormat("yyyy-MM-dd").replacement(numbered("isoLocalDate"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 offset dates, like {@code 2019-02-25+02:00}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 offset dates
   * @see DateTimeFormatter#ISO_OFFSET_DATE
   */
  public static DateTimeScrubber isoOffsetDates() {
    return dateTimeFormat("yyyy-MM-ddXXX").replacement(numbered("isoOffsetDate"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 dates, like {@code 2019-02-25}, or {@code
   * 2019-02-25+02:00}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 dates
   * @see DateTimeFormatter#ISO_DATE
   */
  public static DateTimeScrubber isoDates() {
    return dateTimeFormat("yyyy-MM-dd[XXX]").replacement(numbered("isoDate"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 local times, like {@code 12:34:56}, or {@code
   * 12:34:56.123456789}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 local times
   * @see DateTimeFormatter#ISO_LOCAL_TIME
   */
  public static DateTimeScrubber isoLocalTimes() {
    return dateTimeFormat("HH:mm:ss[.S]").replacement(numbered("isoLocalTime"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 offset times, like {@code 12:34:56+02:00}, or
   * {@code 12:34:56.123456789+02:00}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 offset times
   * @see DateTimeFormatter#ISO_OFFSET_TIME
   */
  public static DateTimeScrubber isoOffsetTimes() {
    return dateTimeFormat("HH:mm:ss[.S]XXX").replacement(numbered("isoOffsetTime"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 times, like {@code 12:34:56}, {@code
   * 12:34:56+02:00}, {@code 12:34:56.123456789}, or {@code 12:34:56.123456789+02:00}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 times
   * @see DateTimeFormatter#ISO_TIME
   */
  public static DateTimeScrubber isoTimes() {
    return dateTimeFormat("HH:mm:ss[.S][XXX]").replacement(numbered("isoTime"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 local date/times, like {@code 12:34:56}, {@code
   * 12:34:56+02:00}, {@code 12:34:56.123456789}, or {@code 12:34:56.123456789+02:00}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 local date/times
   * @see DateTimeFormatter#ISO_LOCAL_DATE_TIME
   */
  public static DateTimeScrubber isoLocalDateTimes() {
    return dateTimeFormat("yyyy-MM-dd'T'HH:mm:ss[.S]").replacement(numbered("isoLocalDateTime"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 offset date/times, like {@code
   * 2019-02-25T12:34:56+02:00}, or {@code 2019-02-25T12:34:56.123456789+02:00}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 offset date/times
   * @see DateTimeFormatter#ISO_OFFSET_DATE_TIME
   */
  public static DateTimeScrubber isoOffsetDateTimes() {
    return dateTimeFormat("yyyy-MM-dd'T'HH:mm:ss[.S]XXX")
        .replacement(numbered("isoOffsetDateTime"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 zoned date/times, like {@code
   * 2019-02-25T12:34:56+02:00}, {@code 2019-02-25T12:34:56.123456789+02:00}, {@code
   * 2019-02-25T12:34:56+02:00[Europe/Berlin]}, or {@code
   * 2019-02-25T12:34:56.123456789+02:00[Europe/Berlin]}.
   *
   * @param locale the {@link Locale} to localize the date/time pattern (influences names for zones
   *     codes)
   * @return a {@link DateTimeScrubber} for ISO-8601 zoned date/times
   * @see DateTimeFormatter#ISO_ZONED_DATE_TIME
   */
  public static DateTimeScrubber isoZonedDateTimes(Locale locale) {
    return dateTimeFormat("yyyy-MM-dd'T'HH:mm:ss[.S]XXX'['VV']'", locale)
        .replacement(numbered("isoZonedDateTime"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 zoned date/times, like {@code
   * 2019-02-25T12:34:56+02:00}, {@code 2019-02-25T12:34:56.123456789+02:00}, {@code
   * 2019-02-25T12:34:56+02:00[Europe/Berlin]}, or {@code
   * 2019-02-25T12:34:56.123456789+02:00[Europe/Berlin]}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 zoned date/times
   * @see #isoZonedDateTimes(Locale)
   */
  public static DateTimeScrubber isoZonedDateTimes() {
    return isoZonedDateTimes(Locale.getDefault());
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 date/times, like {@code 2019-02-25T12:34:56},
   * {@code 2019-02-25T12:34:56.123456789}, {@code 2019-02-25T12:34:56+02:00}, or {@code
   * 2019-02-25T12:34:56.123456789+02:00[Europe/Berlin]}.
   *
   * @param locale the {@link Locale} to localize the date/time pattern (influences names for zones
   *     codes)
   * @return a {@link DateTimeScrubber} for ISO-8601 date/times
   * @see DateTimeFormatter#ISO_DATE_TIME
   */
  public static DateTimeScrubber isoDateTimes(Locale locale) {
    return dateTimeFormat("yyyy-MM-dd'T'HH:mm:ss[.S][XXX['['VV']']]", locale)
        .replacement(numbered("isoDateTime"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 date/times, like {@code 2019-02-25T12:34:56},
   * {@code 2019-02-25T12:34:56.123456789}, {@code 2019-02-25T12:34:56+02:00}, or {@code
   * 2019-02-25T12:34:56.123456789+02:00[Europe/Berlin]}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 date/times
   * @see #isoDateTimes(Locale)
   */
  public static DateTimeScrubber isoDateTimes() {
    return isoDateTimes(Locale.getDefault());
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 ordinal dates, like {@code 2019-86}, or {@code
   * 2019-86+02:00:00}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 ordinal dates
   * @see DateTimeFormatter#ISO_ORDINAL_DATE
   */
  public static DateTimeScrubber isoOrdinalDates() {
    return dateTimeFormat("yyyy-DDD[XXXX]").replacement(numbered("isoOrdinalDate"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 week dates, like {@code 2019-W09-1}, or {@code
   * 2019-W09-1+02:00:00}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 week dates
   * @see DateTimeFormatter#ISO_WEEK_DATE
   */
  public static DateTimeScrubber isoWeekDates() {
    return dateTimeFormat("YYYY-'W'ww-e[XXXX]").replacement(numbered("isoWeekDate"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 instants, like {@code
   * 2019-02-25T12:34:56.123456789+02:00}, or {@code 2019-02-25T12:34:56.123456789Z}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 instants
   * @see DateTimeFormatter#ISO_INSTANT
   */
  public static DateTimeScrubber isoInstants() {
    return dateTimeFormat("uuuu-MM-dd'T'HH:mm:ss.SX").replacement(numbered("isoInstant"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for basic ISO-8601 dates, like {@code 20190225}, or {@code
   * 20190225+02:00}.
   *
   * @return a {@link DateTimeScrubber} for basic ISO-8601 dates
   * @see DateTimeFormatter#BASIC_ISO_DATE
   */
  public static DateTimeScrubber basicIsoDates() {
    return dateTimeFormat("yyyyMMdd[X]").replacement(numbered("basicIsoDate"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for RFC-1123 date/times, like {@code Mon, 35 Feb 2019
   * 12:34:56 GMT}. Note that this always uses the {@link Locale#US}.
   *
   * @return a {@link DateTimeScrubber} for RFC-1123 date/times
   * @see DateTimeFormatter#RFC_1123_DATE_TIME
   */
  public static DateTimeScrubber rfc1123DateTimes() {
    return dateTimeFormat("EEE, d MMM yyyy HH:mm:ss O", Locale.US)
        .replacement(numbered("rfc1123DateTime"));
  }

  /**
   * {@link RegexScrubber} for UUIDs.
   *
   * @return a {@link RegexScrubber} that replaces all UUIDs
   */
  public static RegexScrubber uuids() {
    return stringsMatching(UUID_PATTERN).replacement(numbered("uuid"));
  }
}
