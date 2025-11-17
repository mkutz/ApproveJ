package org.approvej.scrub;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.approvej.scrub.Replacements.string;

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

@NullMarked
record DateTimeScrubberRecord(DateTimeFormatter dateTimeFormatter, RegexScrubber regexScrubber)
    implements DateTimeScrubber {

  DateTimeScrubberRecord(String dateTimePattern, Locale locale, Replacement replacement) {
    this(
        DateTimeFormatter.ofPattern(dateTimePattern, locale),
        new RegexScrubberRecord(Pattern.compile(regexFor(dateTimePattern, locale)), replacement));
  }

  @Override
  public DateTimeScrubber replacement(Replacement replacement) {
    return new DateTimeScrubberRecord(dateTimeFormatter, regexScrubber.replacement(replacement));
  }

  @Override
  public DateTimeScrubber replacement(String staticReplacement) {
    return replacement(string(staticReplacement));
  }

  @Override
  public DateTimeScrubber replacement(RelativeDateTimeReplacement replacement) {
    return new DateTimeScrubberRecord(
        dateTimeFormatter,
        regexScrubber.replacement(replacement.dateTimeFormatter(dateTimeFormatter)));
  }

  @Override
  public DateTimeScrubber replacement(RelativeDateReplacement replacement) {
    return new DateTimeScrubberRecord(
        dateTimeFormatter,
        regexScrubber.replacement(replacement.dateTimeFormatter(dateTimeFormatter)));
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
    YEAR_3(YEAR_4.label, List.of("yyy", "YYY", "uuu"), "-?[0-9][0-9][0-9]"),
    YEAR_2(YEAR_4.label, List.of("yy", "YY", "uu"), "-?[0-9][0-9]"),
    YEAR(YEAR_4.label, List.of("y", "Y", "u"), "-?[0-9]*[0-9]"),
    QUARTER(
        "quarter",
        List.of("QQQQ", "qqqq", "QQQQ", "qqqq", "QQQ", "qqq", "QQ", "qq", "Q", "q"),
        List.of(Month.FEBRUARY, Month.MAY, Month.AUGUST, Month.NOVEMBER)),
    MONTH_LONG("month", List.of("MMMM", "LLLL", "MMM", "LLL"), Arrays.asList(Month.values())),
    MONTH_2(MONTH_LONG.label, List.of("MM", "LL"), "1[0-2]|0[1-9]"),
    MONTH_1(MONTH_LONG.label, List.of("M", "L"), "1[0-2]|[1-9]"),
    WEEK("week", "ww", "5[0-3]|[1-4][0-9]|0[1-9]"),
    WEEK_SHORT(WEEK.label, "w", "5[0-3]|[1-4][0-9]|[1-9]"),
    WEEK_OF_MONTH("weekOfMonth", List.of("W", "F"), "[1-5]"),
    DAY("dayOfMonth", "dd", "3[0-1]|[1-2][0-9]|0[1-9]"),
    DAY_SHORT(DAY.label, "d", "3[0-1]|[1-2][0-9]|[1-9]"),
    DAY_OF_YEAR_3("dayOfYear", "DDD", "36[0-6]|3[0-5][0-9]|[1-2][0-9][0-9]|0[1-9][0-9]|00[1-9]"),
    DAY_OF_YEAR_2(
        DAY_OF_YEAR_3.label, "DD", "36[0-6]|3[0-5][0-9]|[1-2][0-9][0-9]|[1-9][0-9]|0[1-9]"),
    DAY_OF_YEAR_1(DAY_OF_YEAR_3.label, "D", "36[0-6]|3[0-5][0-9]|[1-2][0-9][0-9]|[1-9][0-9]|[1-9]"),
    DAY_OF_WEEK_MAX(
        "dayOfWeek",
        List.of("eeee", "EEEE", "cccc", "eee", "EEE", "ccc", "EE", "E"),
        List.of(DayOfWeek.values())),
    DAY_OF_WEEK_MIDDLE(DAY_OF_WEEK_MAX.label, List.of("ee", "cc"), "0[1-7]"),
    DAY_OF_WEEK_SHORT(DAY_OF_WEEK_MAX.label, List.of("e", "c"), "[1-7]"),
    HOUR("hour", List.of("HH", "kk"), "2[0-3]|1[0-9]|0[0-9]"),
    HOUR_SHORT(HOUR.label, List.of("H", "k"), "2[0-3]|1[0-9]|[0-9]"),
    HOUR_12H(HOUR.label, List.of("hh", "KK"), "1[0-2]|0[0-9]"),
    HOUR_12H_SHORT(HOUR.label, List.of("h", "K"), "1[0-2]|[0-9]"),
    AMPM("ampm", "a", "(?i)am|pm"),
    MINUTE("minute", "mm", "[1-5][0-9]|0[0-9]"),
    MINUTE_SHORT(MINUTE.label, "m", "[1-5][0-9]|[0-9]"),
    SECOND("second", "ss", "[1-5][0-9]|0[0-9]"),
    SECOND_SHORT(SECOND.label, "s", "[1-5][0-9]|[0-9]"),
    FACTION_OF_SECOND("fractionOfSecond", List.of("SSS", "SS", "S"), "[0-9]{1,9}"),
    NANOS("nanos", "n+", "[0-9]+"),
    NANOS_OF_DAY("nanosOfDay", "N+", "[0-9]+"),
    MILLIS_OF_DAY("millisOfDay", "(A+)", "[0-9]+"),
    ZONE_ID_LONG("zoneId", List.of("VV"), "Z|[+-][0-9][0-9]:[0-5][0-9]|[A-Za-z_]+/[A-Za-z_]+"),
    ZONE_OFFSET_MAX("zoneOffset", "ZZZZ", "GMT(([+-][0-9][0-9]:?([0-5][0-9])?:?([0-5][0-9])?)?)"),
    ZONE_OFFSET_LONG(ZONE_OFFSET_MAX.label, List.of("ZZZ", "ZZ", "Z"), "[+-][0-9][0-9][0-5][0-9]"),
    ZONE_OFFSET_Z_MAX(
        ZONE_OFFSET_MAX.label, "XXXX", "Z|[+-][0-9][0-9]:?([0-5][0-9])?:?([0-5][0-9])?"),
    ZONE_OFFSET_Z_LONG(ZONE_OFFSET_MAX.label, List.of("XXX"), "Z|[+-][0-9][0-9]:[0-5][0-9]"),
    ZONE_OFFSET_Z_MIDDLE(ZONE_OFFSET_MAX.label, "XX", "Z|[+-][0-9][0-9][0-5][0-9]"),
    ZONE_OFFSET_Z_SHORT(ZONE_OFFSET_MAX.label, "X", "Z|[+-][0-9][1-9]([0-5][0-9])?"),
    ZONE_OFFSET_x_MAX(
        ZONE_OFFSET_MAX.label, "xxxx", "[+-][0-9][0-9]:?([0-5][0-9])?:?([0-5][0-9])?"),
    ZONE_OFFSET_x_LONG(ZONE_OFFSET_MAX.label, "xxx", "[+-][0-9][0-9]:[0-5][0-9]"),
    ZONE_OFFSET_x_MIDDLE(ZONE_OFFSET_MAX.label, "xx", "[+-][0-9][0-9][0-5][0-9]"),
    ZONE_OFFSET_x_SHORT(ZONE_OFFSET_MAX.label, "x", "[+-][0-9][0-9]([0-5][0-9])?"),
    ZONE_OFFSET_LOCALIZED(ZONE_OFFSET_MAX.label, "OOOO", "GMT([+-](1[1-3]|0[1-9])(:[0-5][0-9])?)?"),
    ZONE_OFFSET_LOCALIZED_SHORT(
        ZONE_OFFSET_MAX.label, "O", "GMT([+-](1[1-3]|[1-9])(:[0-5][0-9])?)?"),
    TEXT("'([^']+)'", "\\\\Q$1\\\\E"),
    OPTIONAL_START("(?<!\\\\)\\[", "("),
    OPTIONAL_END("(?<!\\\\)\\]", ")?"),
    ESCAPE("([#$%^&*().])", "\\\\$1"),
    OTHER("(.)", "$1");

    static final Pattern ANY_FIELD =
        Pattern.compile(stream(values()).map(field -> field.tokenRegex).collect(joining("|")));

    private final String label;
    private final String tokenRegex;
    private final Function<Locale, String> replacementRegexFunction;

    DateTimeToken(String label, String tokenRegex, String replacementRegex) {
      this.label = label;
      this.tokenRegex = tokenRegex;
      this.replacementRegexFunction = locale -> "(?<%s>%s)".formatted(label, replacementRegex);
    }

    DateTimeToken(
        String label, List<String> tokens, Function<Locale, String> replacementRegexFunction) {
      this.label = label;
      this.tokenRegex = String.join("|", tokens);
      this.replacementRegexFunction = replacementRegexFunction;
    }

    DateTimeToken(String tokenRegex, String replacementRegex) {
      this.label = "";
      this.tokenRegex = tokenRegex;
      this.replacementRegexFunction = locale -> replacementRegex;
    }

    DateTimeToken(String label, List<String> tokens, String replacementRegex) {
      this(label, tokens, locale -> "(?<%s>%s)".formatted(label, replacementRegex));
    }

    DateTimeToken(String label, List<String> tokens, List<TemporalAccessor> examples) {
      this(
          label,
          tokens,
          locale ->
              tokens.stream()
                  .map(token -> DateTimeFormatter.ofPattern(token).localizedBy(locale))
                  .flatMap(formatter -> examples.stream().map(formatter::format))
                  .distinct()
                  .collect(joining("|", "(?<%s>".formatted(label), ")")));
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
