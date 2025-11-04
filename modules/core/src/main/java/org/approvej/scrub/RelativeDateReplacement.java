package org.approvej.scrub;

import static java.lang.Math.abs;
import static java.util.stream.Collectors.joining;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
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

  @Override
  public String apply(String match, Integer count) {
    LocalDate parsed = dateTimeFormatter.parse(match, LocalDate::from);
    Period period = Period.between(LocalDate.now(), parsed);
    if (period.isZero()) return "[today]";
    if (period.equals(Period.ofDays(1))) return "[tomorrow]";
    if (period.equals(Period.ofDays(-1))) return "[yesterday]";

    Stream.Builder<String> parts = Stream.builder();

    if (!period.isNegative()) {
      parts.add("in");
    }

    long years = abs(period.getYears());
    if (years != 0) {
      parts.add("%d %s%s".formatted(years, "year", years == 1 ? "" : "s"));
    }

    long months = abs(period.getMonths());
    if (months != 0) {
      parts.add("%d %s%s".formatted(months, "month", months == 1 ? "" : "s"));
    }

    long days = abs(period.getDays());
    if (days != 0) {
      parts.add("%d %s%s".formatted(days, "day", days == 1 ? "" : "s"));
    }

    if (period.isNegative()) {
      parts.add("ago");
    }

    return "[%s]".formatted(parts.build().collect(joining(" ")));
  }
}
