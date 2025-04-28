package org.approvej.scrub;

import static java.util.Arrays.stream;
import static org.approvej.scrub.Replacements.numbered;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;

/** Collection of static methods to create {@link Scrubber} instances. */
@NullMarked
public class Scrubbers {

  public static final Pattern UUID_PATTERN =
      Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

  private Scrubbers() {}

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

  /**
   * {@link RegexScrubber} that replaces instant strings of the given pattern.
   *
   * @return a new {@link RegexScrubber} to scrub instants (e.g. "2023-10-01T12:00:00.000Z")
   */
  public static RegexScrubber instants() {
    return dateTimeFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").replacement(numbered("instant"));
  }

  private enum DateTimeField {
    YEAR_SHORT("([^y]yy[^y]|[^u]uu[^u])", "(?<year>-?[0-9][0-9])"),
    YEAR("(y+|u+)", "(?<year>-?[0-9]*[1-9])"),
    MONTH("(MM|LL)", "(?<month>1[0-2]|0[1-9])"),
    MONTH_SHORT("(M|L)", "(?<month>1[0-2]|[1-9])"),
    DAY("(dd)", "(?<dayOfMonth>3[0-1]|[1-2][0-9]|0[1-9])"),
    DAY_SHORT("(d)", "(?<dayOfMonth>3[0-1]|[1-2][0-9]|[1-9])"),
    DAY_OF_YEAR("(DDD)", "(?<dayOfYear>36[0-6]|3[0-5][0-9]|[1-2][0-9][0-9]|0[1-9][0-9]|00[1-9])"),
    DAY_OF_YEAR_2_DIGITS(
        "(DD)", "(?<dayOfYear>36[0-6]|3[0-5][0-9]|[1-2][0-9][0-9]|[1-9][0-9]|0[1-9])"),
    DAY_OF_YEAR_1_DIGIT(
        "(D)", "(?<dayOfYear>36[0-6]|3[0-5][0-9]|[1-2][0-9][0-9]|[1-9][0-9]|[1-9])"),
    HOUR("(HH)", "(?<hour>2[0-3]|1[0-9]|0[1-9])"),
    HOUR_SHORT("(H)", "(?<hour>2[0-3]|1[0-9]|[1-9])"),
    MINUTE("(mm)", "(?<minute>[1-5][0-9]|0[1-9])"),
    MINUTE_SHORT("(m)", "(?<minute>[1-5][0-9]|[1-9])"),
    SECOND("(ss)", "(?<second>[1-5][0-9]|0[1-9])"),
    SECOND_SHORT("(s)", "(?<second>[1-5][0-9]|[1-9])"),
    FACTION_OF_SECOND("(S+)", "(?<factionOfSecond>[0-9]{1,9})"),
    ZONE_OFFSET_MAX(
        "(ZZZZ+)", "(?<zoneOffset>GMT(([+-][0-9][0-9]:?([0-9][0-9])?:?([0-9][0-9])?)?))"),
    ZONE_OFFSET_LONG("(ZZZ)", "(?<zoneOffset>[+-][0-9][0-9][0-9][0-9])"),
    ZONE_OFFSET_MIDDLE("(ZZ)", "(?<zoneOffset>[+-][0-9][0-9][0-9][0-9])"),
    ZONE_OFFSET_SHORT("(Z)", "(?<zoneOffset>[+-][0-9][0-9][0-9][0-9])"),
    ZONE_OFFSET_Z_MAX("(XXXX+)", "(?<zoneOffset>Z|[+-][0-9][0-9]:?([0-9][0-9])?:?([0-9][0-9])?)"),
    ZONE_OFFSET_Z_LONG("(XXX)", "(?<zoneOffset>Z|[+-][0-9][0-9]:[0-9][0-9])"),
    ZONE_OFFSET_Z_MIDDLE("(XX)", "(?<zoneOffset>Z|[+-][0-9][0-9][0-9][0-9])"),
    ZONE_OFFSET_Z_SHORT("(X)", "(?<zoneOffset>Z|[+-][0-9][1-9])"),
    TEXT("'([^']+)'", "$1"),
    ESCAPE("([#$%^&*().\\[\\]])", "\\\\$1"),
    OTHER("(.)", "$1");
    /*
    Pattern era = Pattern.compile("(G+)"); // text
    Pattern quarter = Pattern.compile("(Q+|q+)");
    Pattern yearWeekBased = Pattern.compile("(Y+)");
    Pattern week = Pattern.compile("(w+)");
    Pattern weekOfMonth = Pattern.compile("(W+|F+)");
    Pattern dayOfWeek = Pattern.compile("(E+)"); // text
    Pattern dayOfWeekLocalized = Pattern.compile("(e+|c+)"); // number/text
    Pattern amPm = Pattern.compile("(a+)"); // text
    Pattern hourAmPmClock = Pattern.compile("(h+)"); // number 1-12
    Pattern hourAmPm = Pattern.compile("(K+)"); // number 0-11
    Pattern hourClock = Pattern.compile("(k+)"); // number 1-24
    Pattern millisOfDay = Pattern.compile("(A+)"); // number
    Pattern nanos = Pattern.compile("(n+)"); // number
    Pattern nanosOfDay = Pattern.compile("(N+)"); // number
    Pattern timeZoneId = Pattern.compile("(V+)"); // text
    Pattern timeZoneName = Pattern.compile("(z+)"); // text
    Pattern timeZoneOffsetLocalized = Pattern.compile("(O+)"); // text
    Pattern timeZoneOffsetZ = Pattern.compile("(X+)"); // text
    Pattern timeZoneOffset = Pattern.compile("(x+|Z+)"); // text
     */

    static final Pattern ANY_FIELD =
        Pattern.compile(
            stream(values()).map(field -> field.regex).collect(Collectors.joining("|")));

    private final String regex;
    private final String replacement;

    DateTimeField(String regex, String replacement) {
      this.regex = regex;
      this.replacement = replacement;
    }

    public static DateTimeField forString(String string) {
      return stream(values())
          .filter(field -> string.matches(field.regex))
          .findFirst()
          .orElseThrow(
              () -> new IllegalArgumentException("No matching field for string: " + string));
    }
  }

  public static RegexScrubber dateTimeFormat(String dateTimePattern) {
    StringBuilder regexBuilder = new StringBuilder();
    Matcher matcher = DateTimeField.ANY_FIELD.matcher(dateTimePattern);
    matcher
        .results()
        .forEach(
            result -> {
              DateTimeField dateTimeField = DateTimeField.forString(result.group());
              regexBuilder.append(
                  result.group().replaceAll(dateTimeField.regex, dateTimeField.replacement));
            });

    return stringsMatching(Pattern.compile(regexBuilder.toString()))
        .replacement(numbered("datetime"));
  }

  private static final ZonedDateTime EXAMPLE_INSTANT = ZonedDateTime.now();

  /**
   * {@link RegexScrubber} that replaces instant strings of the given pattern.
   *
   * @param dateFormatPattern a {@link DateTimeFormatter} to parse the dates
   * @return a new {@link RegexScrubber} with the given {@link DateTimeFormatter} turned into a
   *     {@link Pattern}
   * @deprecated use {@link #instants()} or {@link #dateTimeFormat(String)} instead
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
