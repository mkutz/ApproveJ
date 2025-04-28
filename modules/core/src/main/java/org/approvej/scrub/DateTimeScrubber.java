package org.approvej.scrub;

import static java.util.Arrays.stream;

import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class DateTimeScrubber extends RegexScrubber {

  private String dateTimePattern;

  DateTimeScrubber(String dateTimePattern, Function<Integer, Object> replacement) {
    super(regexFor(dateTimePattern), replacement);
    this.dateTimePattern = dateTimePattern;
  }

  @Override
  public String apply(String input) {
    return super.apply(input);
  }

  private static Pattern regexFor(String dateTimePattern) {
    return Pattern.compile(
        DateTimeField.ANY_FIELD
            .matcher(dateTimePattern)
            .results()
            .map(
                result -> {
                  DateTimeField dateTimeField = DateTimeField.forString(result.group());
                  return result.group().replaceAll(dateTimeField.regex, dateTimeField.replacement);
                })
            .collect(Collectors.joining()));
  }

  private enum DateTimeField {
    ERA_MAX("(GGGG+)", "(?<era>-?Anno Domini|Before Christ)"), // TODO very locale specific
    ERA_LONG("(GGG)", "(?<era>AD|BC)"), // TODO very locale specific
    ERA_MIDDLE("(GG)", "(?<era>AD|BC)"), // TODO very locale specific
    ERA_SHORT("(G)", "(?<era>AD|BC)"), // TODO very locale specific
    YEAR_SHORT("([^y]yy[^y]|[^Y]YY[^Y]|[^u]uu[^u])", "(?<year>-?[0-9][0-9])"),
    YEAR("(y+|Y+|u+)", "(?<year>-?[0-9]*[1-9])"),
    QUARTER_MAX(
        "(QQQQ+|qqqq+)", "(?<quarter>(1st|2nd|3rd|4th) quarter)"), // TODO very locale specific
    QUARTER_LONG("(QQQ|qqq)", "(?<quarter>Q[1-4])"),
    QUARTER_MIDDLE("(QQ|qq)", "(?<quarter>0[1-4])"),
    QUARTER_SHORT("(Q|q)", "(?<quarter>[1-4])"),
    MONTH_MAX(
        "(MMMM+|LLLL+)",
        "(?<month>January|February|March|April|Mai|June|July|August|September|October|November|December)"), // TODO very locale specific
    MONTH_LONG(
        "(MMM|LLL)",
        "(?<month>Jan|Feb|Mar|Apr|Mai|Jun|Jul|Aug|Sep|Oct|Nov|Dec)"), // TODO very locale specific
    MONTH_MIDDLE("(MM|LL)", "(?<month>1[0-2]|0[1-9])"),
    MONTH_SHORT("(M|L)", "(?<month>1[0-2]|[1-9])"),
    WEEK("(ww)", "(?<week>5[0-3]|[1-4][0-9]|0[1-9])"),
    WEEK_SHORT("(w)", "(?<week>5[0-3]|[1-4][0-9]|[1-9])"),
    WEEK_OF_MONTH("(W|F)", "(?<weekOfMonth>[1-5])"),
    DAY("(dd)", "(?<dayOfMonth>3[0-1]|[1-2][0-9]|0[1-9])"),
    DAY_SHORT("(d)", "(?<dayOfMonth>3[0-1]|[1-2][0-9]|[1-9])"),
    DAY_OF_YEAR("(DDD)", "(?<dayOfYear>36[0-6]|3[0-5][0-9]|[1-2][0-9][0-9]|0[1-9][0-9]|00[1-9])"),
    DAY_OF_YEAR_2_DIGITS(
        "(DD)", "(?<dayOfYear>36[0-6]|3[0-5][0-9]|[1-2][0-9][0-9]|[1-9][0-9]|0[1-9])"),
    DAY_OF_YEAR_1_DIGIT(
        "(D)", "(?<dayOfYear>36[0-6]|3[0-5][0-9]|[1-2][0-9][0-9]|[1-9][0-9]|[1-9])"),
    DAY_OF_WEEK_MAX(
        "(eeee+|EEEE+|cccc+)",
        "(?<dayOfWeek>Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)"), // TODO very
    // locale
    // specific
    DAY_OF_WEEK_LONG(
        "(eee|EEE|EE|E|ccc)",
        "(?<dayOfWeek>Mon|Tue|Wed|Thu|Fri|Sat|Sun)"), // TODO very locale specific
    DAY_OF_WEEK_MIDDLE("(ee|cc)", "(?<dayOfWeek>0[1-7])"),
    DAY_OF_WEEK_SHORT("(e|c)", "(?<dayOfWeek>[1-7])"),
    HOUR("(HH|kk)", "(?<hour>2[0-3]|1[0-9]|0[1-9])"),
    HOUR_SHORT("(H|k)", "(?<hour>2[0-3]|1[0-9]|[1-9])"),
    HOUR_12H("(hh|KK)", "(?<hour>1[0-2]|0[1-9])"),
    HOUR_12H_SHORT("(h|K)", "(?<hour>1[0-2]|[1-9])"),
    AMPM("(a)", "(?<ampm>AM|PM)"),
    MINUTE("(mm)", "(?<minute>[1-5][0-9]|0[1-9])"),
    MINUTE_SHORT("(m)", "(?<minute>[1-5][0-9]|[1-9])"),
    SECOND("(ss)", "(?<second>[1-5][0-9]|0[1-9])"),
    SECOND_SHORT("(s)", "(?<second>[1-5][0-9]|[1-9])"),
    FACTION_OF_SECOND("(S+)", "(?<factionOfSecond>[0-9]{1,9})"),
    NANOS("(n+)", "(?<nanos>[0-9]+)"),
    NANOS_OF_DAY("(N+)", "(?<nanosOfDay>[0-9]+)"),
    MILLIS_OF_DAY("(A+)", "(?<millisOfDay>[0-9]+)"),
    ZONE_OFFSET_MAX(
        "(ZZZZ+)", "(?<zoneOffset>GMT(([+-][0-9][0-9]:?([0-5][0-9])?:?([0-5][0-9])?)?))"),
    ZONE_OFFSET_LONG("(ZZZ)", "(?<zoneOffset>[+-][0-9][0-9][0-5][0-9])"),
    ZONE_OFFSET_MIDDLE("(ZZ)", "(?<zoneOffset>[+-][0-9][0-9][0-5][0-9])"),
    ZONE_OFFSET_SHORT("(Z)", "(?<zoneOffset>[+-][0-9][0-9][0-5][0-9])"),
    ZONE_OFFSET_Z_MAX("(XXXX+)", "(?<zoneOffset>Z|[+-][0-9][0-9]:?([0-5][0-9])?:?([0-5][0-9])?)"),
    ZONE_OFFSET_Z_LONG("(XXX|VV)", "(?<zoneOffset>Z|[+-][0-9][0-9]:[0-5][0-9])"),
    ZONE_OFFSET_Z_MIDDLE("(XX)", "(?<zoneOffset>Z|[+-][0-9][0-9][0-5][0-9])"),
    ZONE_OFFSET_Z_SHORT("(X)", "(?<zoneOffset>Z|[+-][0-9][1-9]([0-5][0-9])?)"),
    ZONE_OFFSET_x_MAX("(xxxx+)", "(?<zoneOffset>[+-][0-9][0-9]:?([0-5][0-9])?:?([0-5][0-9])?)"),
    ZONE_OFFSET_x_LONG("(xxx)", "(?<zoneOffset>[+-][0-9][0-9]:[0-5][0-9])"),
    ZONE_OFFSET_x_MIDDLE("(xx)", "(?<zoneOffset>[+-][0-9][0-9][0-5][0-9])"),
    ZONE_OFFSET_x_SHORT("(x)", "(?<zoneOffset>[+-][0-9][0-9]([0-5][0-9])?)"),
    ZONE_OFFSET_LOCALIZED("(OOOO)", "(?<zoneOffset>GMT([+-](1[1-3]|0[1-9])(:[0-5][0-9])?)?)"),
    ZONE_OFFSET_LOCALIZED_SHORT("(O)", "(?<zoneOffset>GMT([+-](1[1-3]|[1-9])(:[0-5][0-9])?)?)"),
    TEXT("'([^']+)'", "$1"),
    ESCAPE("([#$%^&*().\\[\\]])", "\\\\$1"),
    OTHER("(.)", "$1");

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
}
