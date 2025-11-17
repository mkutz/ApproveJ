package org.approvej.scrub;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.YEARS;
import static java.util.stream.Collectors.joining;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;

@NullMarked
record RelativeDateReplacementRecord(DateTimeFormatter dateTimeFormatter)
    implements RelativeDateReplacement {

  @Override
  public RelativeDateReplacement dateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
    return new RelativeDateReplacementRecord(dateTimeFormatter);
  }

  @Override
  public String apply(String match, Integer count) {
    LocalDate parsed = dateTimeFormatter.parse(match, LocalDate::from);
    Period period = Period.between(LocalDate.now(), parsed);
    Period absolute = period.isNegative() ? period.negated() : period;

    if (period.isZero()) return "[today]";
    if (period.equals(Period.ofDays(1))) return "[tomorrow]";
    if (period.equals(Period.ofDays(-1))) return "[yesterday]";

    return "[%s]"
        .formatted(
            Stream.of(
                    !period.isNegative() ? "in" : "",
                    printPart(absolute.getYears(), YEARS),
                    printPart(absolute.getMonths(), MONTHS),
                    printPart(absolute.getDays(), DAYS),
                    period.isNegative() ? "ago" : "")
                .filter(string -> !string.isBlank())
                .collect(joining(" ")));
  }

  private static String printPart(long value, ChronoUnit unit) {
    if (value == 0) {
      return "";
    }
    return "%d %s%s"
        .formatted(
            value,
            switch (unit) {
              case YEARS -> "year";
              case MONTHS -> "month";
              case DAYS -> "day";
              default -> "";
            },
            value > 1 ? "s" : "");
  }
}
