package org.approvej.scrub;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.approvej.scrub.Replacements.relativeDate;
import static org.approvej.scrub.Replacements.relativeDateTime;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.jspecify.annotations.NullMarked;

/**
 * Scrubs a {@link String} of date/time strings described by a {@link DateTimeFormatter} pattern
 * like "yyyy-MM-dd" for local dates using the wrapped {@link RegexScrubber}.
 */
@NullMarked
public class DateTimeScrubber implements Scrubber<String> {

  private final DateTimeFormatter dateTimeFormatter;
  private final RegexScrubber regexScrubber;

  /**
   * Creates a {@link DateTimeScrubber} to scrub date/time strings of the given dateTimeFormatter.
   *
   * @param dateTimeFormatter a {@link DateTimeFormatter} for the pattern that should be scrubbed
   * @param regexScrubber a wrapped {@link RegexScrubber} that does the actual scrubbing
   */
  DateTimeScrubber(DateTimeFormatter dateTimeFormatter, RegexScrubber regexScrubber) {
    this.dateTimeFormatter = dateTimeFormatter;
    this.regexScrubber = regexScrubber;
  }

  /**
   * Creates a {@link DateTimeScrubber} to scrub date/time strings of the given dateTimePattern.
   *
   * @param dateTimePattern a date/time pattern as used by {@link DateTimeFormatter}
   * @param locale the {@link Locale} for the date/time pattern (influences names of months or
   *     weekdays for example)
   * @param replacement the {@link Replacement} function
   * @see DateTimeFormatter
   */
  DateTimeScrubber(String dateTimePattern, Locale locale, Replacement replacement) {
    this(
        DateTimeFormatter.ofPattern(dateTimePattern, locale),
        new RegexScrubber(Pattern.compile(regexFor(dateTimePattern, locale)), replacement));
  }

  /**
   * Set the {@link Replacement} to be used.
   *
   * @param replacement a {@link Replacement} function
   * @return this
   */
  public DateTimeScrubber replacement(Replacement replacement) {
    return new DateTimeScrubber(dateTimeFormatter, regexScrubber.replacement(replacement));
  }

  /**
   * Set the replacement {@link Function} always returning the given staticReplacement.
   *
   * @param staticReplacement the static replacement {@link String}
   * @return this
   */
  public DateTimeScrubber replacement(String staticReplacement) {
    return new DateTimeScrubber(dateTimeFormatter, regexScrubber.replacement(staticReplacement));
  }

  /**
   * Makes this use a {@link Replacements#relativeDate(String) relativeDate} to replace matches of
   * the date/time pattern.
   *
   * @return this
   */
  public DateTimeScrubber replaceWithRelativeDate() {
    return new DateTimeScrubber(
        dateTimeFormatter, regexScrubber.replacement(relativeDate(dateTimeFormatter)));
  }

  /**
   * Makes this use a {@link Replacements#relativeDateTime(String) relativeDateTime} to replace
   * matches of the date/time pattern.
   *
   * @return this
   */
  public DateTimeScrubber replaceWithRelativeDateTime() {
    return new DateTimeScrubber(
        dateTimeFormatter, regexScrubber.replacement(relativeDateTime(dateTimeFormatter)));
  }

  @Override
  public String apply(String value) {
    return regexScrubber.apply(value);
  }

  private static String regexFor(String dateTimePattern, Locale locale) {
    return DateTimeToken.ANY_FIELD
        .matcher(dateTimePattern)
        .results()
        .map(
            result -> {
              DateTimeToken dateTimeToken = DateTimeToken.forString(result.group());
              return result
                  .group()
                  .replaceAll(dateTimeToken.tokenRegex, dateTimeToken.replacementRegex(locale));
            })
        .collect(joining());
  }

  private enum DateTimeToken {
    ERA("era", List.of("GGGG", "GGG", "GG", "G"), List.of(Year.of(-1), Year.of(1))),
    YEAR_4("year", List.of("yyyy", "YYYY", "uuuu"), "-?[0-9][0-9][0-9][0-9]"),
    YEAR_3("year", List.of("yyy", "YYY", "uuu"), "-?[0-9][0-9][0-9]"),
    YEAR_2("year", List.of("yy", "YY", "uu"), "-?[0-9][0-9]"),
    YEAR("year", List.of("y", "Y", "u"), "-?[0-9]*[0-9]"),
    QUARTER(
        "quarter",
        List.of("QQQQ", "qqqq", "QQQQ", "qqqq", "QQQ", "qqq", "QQ", "qq", "Q", "q"),
        List.of(Month.FEBRUARY, Month.MAY, Month.AUGUST, Month.NOVEMBER)),
    MONTH_LONG("month", List.of("MMMM", "LLLL", "MMM", "LLL"), Arrays.asList(Month.values())),
    MONTH_2("month", List.of("MM", "LL"), "1[0-2]|0[1-9]"),
    MONTH_1("month", List.of("M", "L"), "1[0-2]|[1-9]"),
    WEEK("week", "ww", "5[0-3]|[1-4][0-9]|0[1-9]"),
    WEEK_SHORT("week", "w", "5[0-3]|[1-4][0-9]|[1-9]"),
    WEEK_OF_MONTH("weekOfMonth", List.of("W", "F"), "[1-5]"),
    DAY("dayOfMonth", "dd", "3[0-1]|[1-2][0-9]|0[1-9]"),
    DAY_SHORT("dayOfMonth", "d", "3[0-1]|[1-2][0-9]|[1-9]"),
    DAY_OF_YEAR_3("dayOfYear", "DDD", "36[0-6]|3[0-5][0-9]|[1-2][0-9][0-9]|0[1-9][0-9]|00[1-9]"),
    DAY_OF_YEAR_2("dayOfYear", "DD", "36[0-6]|3[0-5][0-9]|[1-2][0-9][0-9]|[1-9][0-9]|0[1-9]"),
    DAY_OF_YEAR_1("dayOfYear", "D", "36[0-6]|3[0-5][0-9]|[1-2][0-9][0-9]|[1-9][0-9]|[1-9]"),
    DAY_OF_WEEK_MAX(
        "dayOfWeek",
        List.of("eeee", "EEEE", "cccc", "eee", "EEE", "ccc", "EE", "E"),
        List.of(DayOfWeek.values())),
    DAY_OF_WEEK_MIDDLE("dayOfWeek", List.of("ee", "cc"), "0[1-7]"),
    DAY_OF_WEEK_SHORT("dayOfWeek", List.of("e", "c"), "[1-7]"),
    HOUR("hour", List.of("HH", "kk"), "2[0-3]|1[0-9]|0[0-9]"),
    HOUR_SHORT("hour", List.of("H", "k"), "2[0-3]|1[0-9]|[0-9]"),
    HOUR_12H("hour", List.of("hh", "KK"), "1[0-2]|0[0-9]"),
    HOUR_12H_SHORT("hour", List.of("h", "K"), "1[0-2]|[0-9]"),
    AMPM("ampm", "a", "(?i)am|pm"),
    MINUTE("minute", "mm", "[1-5][0-9]|0[0-9]"),
    MINUTE_SHORT("minute", "m", "[1-5][0-9]|[0-9]"),
    SECOND("second", "ss", "[1-5][0-9]|0[0-9]"),
    SECOND_SHORT("second", "s", "[1-5][0-9]|[0-9]"),
    FACTION_OF_SECOND("fractionOfSecond", List.of("SSS", "SS", "S"), "[0-9]{1,9}"),
    NANOS("nanos", "n+", "[0-9]+"),
    NANOS_OF_DAY("nanosOfDay", "N+", "[0-9]+"),
    MILLIS_OF_DAY("millisOfDay", "(A+)", "[0-9]+"),
    ZONE_ID_LONG("zoneId", List.of("VV"), "Z|[+-][0-9][0-9]:[0-5][0-9]|[A-Za-z_]+/[A-Za-z_]+"),
    ZONE_OFFSET_MAX("zoneOffset", "ZZZZ", "GMT(([+-][0-9][0-9]:?([0-5][0-9])?:?([0-5][0-9])?)?)"),
    ZONE_OFFSET_LONG("zoneOffset", List.of("ZZZ", "ZZ", "Z"), "[+-][0-9][0-9][0-5][0-9]"),
    ZONE_OFFSET_Z_MAX("zoneOffset", "XXXX", "Z|[+-][0-9][0-9]:?([0-5][0-9])?:?([0-5][0-9])?"),
    ZONE_OFFSET_Z_LONG("zoneOffset", List.of("XXX"), "Z|[+-][0-9][0-9]:[0-5][0-9]"),
    ZONE_OFFSET_Z_MIDDLE("zoneOffset", "XX", "Z|[+-][0-9][0-9][0-5][0-9]"),
    ZONE_OFFSET_Z_SHORT("zoneOffset", "X", "Z|[+-][0-9][1-9]([0-5][0-9])?"),
    ZONE_OFFSET_x_MAX("zoneOffset", "xxxx", "[+-][0-9][0-9]:?([0-5][0-9])?:?([0-5][0-9])?"),
    ZONE_OFFSET_x_LONG("zoneOffset", "xxx", "[+-][0-9][0-9]:[0-5][0-9]"),
    ZONE_OFFSET_x_MIDDLE("zoneOffset", "xx", "[+-][0-9][0-9][0-5][0-9]"),
    ZONE_OFFSET_x_SHORT("zoneOffset", "x", "[+-][0-9][0-9]([0-5][0-9])?"),
    ZONE_OFFSET_LOCALIZED("zoneOffset", "OOOO", "GMT([+-](1[1-3]|0[1-9])(:[0-5][0-9])?)?"),
    ZONE_OFFSET_LOCALIZED_SHORT("zoneOffset", "O", "GMT([+-](1[1-3]|[1-9])(:[0-5][0-9])?)?"),
    TEXT("'([^']+)'", "\\\\Q$1\\\\E"),
    OPTIONAL_START("(?<!\\\\)\\[", "("),
    OPTIONAL_END("(?<!\\\\)\\]", ")?"),
    ESCAPE("([#$%^&*().])", "\\\\$1"),
    OTHER("(.)", "$1");

    static final Pattern ANY_FIELD =
        Pattern.compile(stream(values()).map(field -> field.tokenRegex).collect(joining("|")));

    private final String tokenRegex;
    private final Function<Locale, String> replacementRegexFunction;

    DateTimeToken(String name, String tokenRegex, String replacementRegex) {
      this.tokenRegex = tokenRegex;
      this.replacementRegexFunction = locale -> "(?<%s>%s)".formatted(name, replacementRegex);
    }

    DateTimeToken(List<String> tokens, Function<Locale, String> replacementRegexFunction) {
      this.tokenRegex = String.join("|", tokens);
      this.replacementRegexFunction = replacementRegexFunction;
    }

    DateTimeToken(String tokenRegex, String replacementRegex) {
      this.tokenRegex = tokenRegex;
      this.replacementRegexFunction = locale -> replacementRegex;
    }

    DateTimeToken(String name, List<String> tokens, String replacementRegex) {
      this(tokens, locale -> "(?<%s>%s)".formatted(name, replacementRegex));
    }

    DateTimeToken(String name, List<String> tokens, List<TemporalAccessor> examples) {
      this(
          tokens,
          locale ->
              tokens.stream()
                  .map(token -> DateTimeFormatter.ofPattern(token).localizedBy(locale))
                  .flatMap(formatter -> examples.stream().map(formatter::format))
                  .distinct()
                  .collect(joining("|", "(?<%s>".formatted(name), ")")));
    }

    String replacementRegex(Locale locale) {
      return replacementRegexFunction.apply(locale);
    }

    public static DateTimeToken forString(String string) {
      return stream(values())
          .filter(field -> string.matches(field.tokenRegex))
          .findFirst()
          .orElse(OTHER);
    }
  }
}
