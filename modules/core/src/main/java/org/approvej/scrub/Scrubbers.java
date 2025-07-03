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
   * Creates a {@link DateTimeScrubber} for ISO-8601 local dates, like {@code 2019-02-25}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 local dates
   * @see DateTimeFormatter#ISO_LOCAL_DATE
   */
  public static RegexScrubber isoLocalDates() {
    return dateTimeFormat("yyyy-MM-dd").replacement(numbered("isoLocalDate"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 offset dates, like {@code 2019-02-25+02:00}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 offset dates
   * @see DateTimeFormatter#ISO_OFFSET_DATE
   */
  public static RegexScrubber isoOffsetDates() {
    return dateTimeFormat("yyyy-MM-ddXXX").replacement(numbered("isoOffsetDate"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 dates, like {@code 2019-02-25}, or {@code
   * 2019-02-25+02:00}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 dates
   * @see DateTimeFormatter#ISO_DATE
   */
  public static RegexScrubber isoDates() {
    return dateTimeFormat("yyyy-MM-dd[XXX]").replacement(numbered("isoDate"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 local times, like {@code 12:34:56}, or {@code
   * 12:34:56.123456789}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 local times
   * @see DateTimeFormatter#ISO_LOCAL_TIME
   */
  public static RegexScrubber isoLocalTimes() {
    return dateTimeFormat("HH:mm:ss[.S]").replacement(numbered("isoLocalTime"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 offset times, like {@code 12:34:56+02:00}, or
   * {@code 12:34:56.123456789+02:00}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 offset times
   * @see DateTimeFormatter#ISO_OFFSET_TIME
   */
  public static RegexScrubber isoOffsetTimes() {
    return dateTimeFormat("HH:mm:ss[.S]XXX").replacement(numbered("isoOffsetTime"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 times, like {@code 12:34:56}, {@code
   * 12:34:56+02:00}, {@code 12:34:56.123456789}, or {@code 12:34:56.123456789+02:00}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 times
   * @see DateTimeFormatter#ISO_TIME
   */
  public static RegexScrubber isoTimes() {
    return dateTimeFormat("HH:mm:ss[.S][XXX]").replacement(numbered("isoTime"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 local date/times, like {@code 12:34:56}, {@code
   * 12:34:56+02:00}, {@code 12:34:56.123456789}, or {@code 12:34:56.123456789+02:00}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 local date/times
   * @see DateTimeFormatter#ISO_LOCAL_DATE_TIME
   */
  public static RegexScrubber isoLocalDateTimes() {
    return dateTimeFormat("yyyy-MM-dd'T'HH:mm:ss[.S]").replacement(numbered("isoLocalDateTime"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 offset date/times, like {@code
   * 2019-02-25T12:34:56+02:00}, or {@code 2019-02-25T12:34:56.123456789+02:00}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 offset date/times
   * @see DateTimeFormatter#ISO_OFFSET_DATE_TIME
   */
  public static RegexScrubber isoOffsetDateTimes() {
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
  public static RegexScrubber isoZonedDateTimes(Locale locale) {
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
  public static RegexScrubber isoZonedDateTimes() {
    return isoZonedDateTimes(Locale.getDefault());
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 date/times, like {@code 2019-02-25T12:34:56},
   * {@code 2019-02-25T12:34:56.123456789}, {@code 2019-02-25T12:34:56+02:00}, or {@code
   * 2019-02-25T12:34:56.123456789+02:00[Europe/Berlin]}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 date/times
   * @see DateTimeFormatter#ISO_DATE_TIME
   */
  public static RegexScrubber isoDateTimes(Locale locale) {
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
  public static RegexScrubber isoDateTimes() {
    return isoDateTimes(Locale.getDefault());
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 ordinal dates, like {@code 2019-86}, or {@code
   * 2019-86+02:00:00}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 ordinal dates
   * @see DateTimeFormatter#ISO_ORDINAL_DATE
   */
  public static RegexScrubber isoOrdinalDates() {
    return dateTimeFormat("yyyy-DDD[XXXX]").replacement(numbered("isoOrdinalDate"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 week dates, like {@code 2019-W09-1}, or {@code
   * 2019-W09-1+02:00:00}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 week dates
   * @see DateTimeFormatter#ISO_WEEK_DATE
   */
  public static RegexScrubber isoWeekDates() {
    return dateTimeFormat("YYYY-'W'ww-e[XXXX]").replacement(numbered("isoWeekDate"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for ISO-8601 instants, like {@code
   * 2019-02-25T12:34:56.123456789+02:00}, or {@code 2019-02-25T12:34:56.123456789Z}.
   *
   * @return a {@link DateTimeScrubber} for ISO-8601 instants
   * @see DateTimeFormatter#ISO_INSTANT
   */
  public static RegexScrubber isoInstants() {
    return dateTimeFormat("uuuu-MM-dd'T'HH:mm:ss.SX").replacement(numbered("isoInstant"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for basic ISO-8601 dates, like {@code 20190225}, or {@code
   * 20190225+02:00}.
   *
   * @return a {@link DateTimeScrubber} for basic ISO-8601 dates
   * @see DateTimeFormatter#BASIC_ISO_DATE
   */
  public static RegexScrubber basicIsoDates() {
    return dateTimeFormat("yyyyMMdd[X]").replacement(numbered("basicIsoDate"));
  }

  /**
   * Creates a {@link DateTimeScrubber} for RFC-1123 date/times, like {@code Mon, 35 Feb 2019
   * 12:34:56 GMT}. Note that this always uses the {@link Locale#US}.
   *
   * @return a {@link DateTimeScrubber} for RFC-1123 date/times
   * @see DateTimeFormatter#RFC_1123_DATE_TIME
   */
  public static RegexScrubber rfc1123DateTimes() {
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

  /**
   * Creates a {@link Scrubber} to replace date strings of the given pattern with relative
   * descriptions, like {@code [today]}, {@code [yesterday]}, {@code [2 days from now]} , {@code [21
   * days ago]}.
   *
   * @param dateFormatPattern a {@link DateTimeFormatter} to parse the dates
   * @return a new {@link RelativeDateScrubber} with the given {@link DateTimeFormatter}.
   */
  public static RelativeDateScrubber relativeDates(DateTimeFormatter dateFormatPattern) {
    return new RelativeDateScrubber(dateFormatPattern);
  }
}
