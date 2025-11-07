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

/**
 * Replaces each match of the given dateTimePattern (as defined by {@link DateTimeFormatter}) with a
 * relative description, like {@code [today]}, {@code [yesterday]}, {@code [13 days from now]}.
 */
public class RelativeDateReplacement implements Replacement {

  private final DateTimeFormatter dateTimeFormatter;

  RelativeDateReplacement(DateTimeFormatter dateTimeFormatter) {
    this.dateTimeFormatter = dateTimeFormatter;
  }

  /**
   * Creates and returns a copy of this with the given dateTimeFormatter.
   *
   * @param dateTimeFormatter the {@link DateTimeFormatter} to be used
   * @return a copy of this with the given dateTimeFormatter
   */
  public RelativeDateReplacement dateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
    return new RelativeDateReplacement(dateTimeFormatter);
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
