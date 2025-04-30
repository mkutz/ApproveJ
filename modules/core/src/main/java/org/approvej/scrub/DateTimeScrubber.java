package org.approvej.scrub;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

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
 * Special {@link RegexScrubber} to scrub date/time strings described by a {@link DateTimeFormatter}
 * pattern like "yyyy-MM-dd" for local dates.
 */
@NullMarked
public class DateTimeScrubber extends RegexScrubber {

  /**
   * Creates a {@link DateTimeScrubber} to scrub date/time strings of the given dateTimePattern.
   *
   * @param dateTimePattern a date/time pattern as used by {@link
   *     java.time.format.DateTimeFormatter}
   * @param locale the locale for the date/time pattern (influences names of moths or weekdays for
   *     example)
   * @param replacement a function that receives the finding index and returns the replacement
   *     string
   * @see java.time.format.DateTimeFormatter
   */
  DateTimeScrubber(String dateTimePattern, Locale locale, Function<Integer, Object> replacement) {
    super(Pattern.compile(regexFor(dateTimePattern, locale)), replacement);
  }

  private static String regexFor(String dateTimePattern, Locale locale) {
    return DateTimeToken.ANY_FIELD
        .matcher(dateTimePattern)
        .results()
        .map(
            result -> {
              DateTimeToken dateTimeToken = DateTimeToken.forString(result.group());
              if (dateTimeToken == DateTimeToken.OPTIONAL) {
                return "(%s)?"
                    .formatted(
                        regexFor(
                            result
                                .group()
                                .replaceAll(
                                    dateTimeToken.tokenRegex,
                                    dateTimeToken.replacementRegex(locale)),
                            locale));
              }
              return result
                  .group()
                  .replaceAll(dateTimeToken.tokenRegex, dateTimeToken.replacementRegex(locale));
            })
        .collect(joining());
  }

  private enum DateTimeToken {
    ERA("era", List.of("GGGG", "GGG", "GG", "G"), List.of(Year.of(-1), Year.of(1))),
    YEAR_4("year", List.of("yyyy", "YYYY", "uuuu"), "-?[0-9][0-9][0-9][1-9]"),
    YEAR_3("year", List.of("yyy", "YYY", "uuu"), "-?[0-9][0-9][1-9]"),
    YEAR_2("year", List.of("yy", "YY", "uu"), "-?[0-9][1-9]"),
    YEAR("year", List.of("y", "Y", "u"), "-?[0-9]*[1-9]"),
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
    HOUR("hour", List.of("HH", "kk"), "2[0-3]|1[0-9]|0[1-9]"),
    HOUR_SHORT("hour", List.of("H", "k"), "2[0-3]|1[0-9]|[1-9]"),
    HOUR_12H("hour", List.of("hh", "KK"), "1[0-2]|0[1-9]"),
    HOUR_12H_SHORT("hour", List.of("h", "K"), "1[0-2]|[1-9]"),
    AMPM("ampm", "a", "AM|PM"),
    MINUTE("minute", "mm", "[1-5][0-9]|0[1-9]"),
    MINUTE_SHORT("minute", "m", "[1-5][0-9]|[1-9]"),
    SECOND("second", "ss", "[1-5][0-9]|0[1-9]"),
    SECOND_SHORT("second", "s", "[1-5][0-9]|[1-9]"),
    FACTION_OF_SECOND("fractionOfSecond", List.of("SSS", "SS", "S"), "[0-9]{1,9}"),
    NANOS("nanos", "n+", "[0-9]+"),
    NANOS_OF_DAY("nanosOfDay", "N+", "[0-9]+"),
    MILLIS_OF_DAY("millisOfDay", "(A+)", "[0-9]+"),
    ZONE_OFFSET_MAX("zoneOffset", "ZZZZ", "GMT(([+-][0-9][0-9]:?([0-5][0-9])?:?([0-5][0-9])?)?)"),
    ZONE_OFFSET_LONG("zoneOffset", List.of("ZZZ", "ZZ", "Z"), "[+-][0-9][0-9][0-5][0-9]"),
    ZONE_OFFSET_Z_MAX("zoneOffset", "XXXX", "Z|[+-][0-9][0-9]:?([0-5][0-9])?:?([0-5][0-9])?"),
    ZONE_OFFSET_Z_LONG("zoneOffset", List.of("XXX", "VV"), "Z|[+-][0-9][0-9]:[0-5][0-9]"),
    ZONE_OFFSET_Z_MIDDLE("zoneOffset", "XX", "Z|[+-][0-9][0-9][0-5][0-9]"),
    ZONE_OFFSET_Z_SHORT("zoneOffset", "X", "Z|[+-][0-9][1-9]([0-5][0-9])?"),
    ZONE_OFFSET_x_MAX("zoneOffset", "xxxx", "[+-][0-9][0-9]:?([0-5][0-9])?:?([0-5][0-9])?"),
    ZONE_OFFSET_x_LONG("zoneOffset", "xxx", "[+-][0-9][0-9]:[0-5][0-9]"),
    ZONE_OFFSET_x_MIDDLE("zoneOffset", "xx", "[+-][0-9][0-9][0-5][0-9]"),
    ZONE_OFFSET_x_SHORT("zoneOffset", "x", "[+-][0-9][0-9]([0-5][0-9])?"),
    ZONE_OFFSET_LOCALIZED("zoneOffset", "OOOO", "GMT([+-](1[1-3]|0[1-9])(:[0-5][0-9])?)?"),
    ZONE_OFFSET_LOCALIZED_SHORT("zoneOffset", "O", "GMT([+-](1[1-3]|[1-9])(:[0-5][0-9])?)?"),
    TEXT("'([^']+)'", "$1"),
    OPTIONAL("\\[([^\\]]+)\\]", "$1"),
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
