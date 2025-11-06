package org.approvej.scrub;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.stream.Collectors.joining;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Replaces each match of the given dateTimePattern (as defined by {@link DateTimeFormatter}) with a
 * relative description, like {@code [10m ago]}, {@code [in 2d 1h 3m 33s]}, {@code [13d ago]}.
 *
 * <p>The value is rounded to full seconds.
 */
public class RelativeDateTimeReplacement implements Replacement {

  private final DateTimeFormatter dateTimeFormatter;
  private final ChronoUnit roundToUnit;

  RelativeDateTimeReplacement(DateTimeFormatter dateTimeFormatter, ChronoUnit roundToUnit) {
    this.dateTimeFormatter = dateTimeFormatter;
    this.roundToUnit = roundToUnit;
  }

  /**
   * Rounds the relative date to the wholes of the given unit.
   *
   * @param unit the {@link ChronoUnit} to round to
   * @return this
   */
  public RelativeDateTimeReplacement roundToWhole(ChronoUnit unit) {
    return new RelativeDateTimeReplacement(dateTimeFormatter, unit);
  }

  @Override
  public String apply(String match, Integer count) {
    ZonedDateTime parsed = dateTimeFormatter.parse(match, ZonedDateTime::from);
    Duration rounded = roundToUnit(Duration.between(ZonedDateTime.now(), parsed), roundToUnit);
    Duration absolute = rounded.abs();

    if (rounded.isZero()) {
      return "[now]";
    }

    return "[%s]"
        .formatted(
            Stream.of(
                    rounded.isPositive() ? "in" : null,
                    printPart(absolute.toDaysPart(), DAYS),
                    printPart(absolute.toHoursPart(), HOURS),
                    printPart(absolute.toMinutesPart(), MINUTES),
                    printPart(absolute.toSecondsPart(), SECONDS),
                    rounded.isNegative() ? "ago" : null)
                .filter(Objects::nonNull)
                .collect(joining(" ")));
  }

  private static Duration roundToUnit(Duration duration, ChronoUnit unit) {
    Duration roundedAbsolute =
        duration.abs().plus(unit.getDuration().dividedBy(2)).truncatedTo(unit);
    if (duration.isNegative()) {
      return roundedAbsolute.negated();
    }
    return roundedAbsolute;
  }

  private static String printPart(long value, ChronoUnit unit) {
    if (value == 0) {
      return null;
    }
    return "%d%s"
        .formatted(
            value,
            switch (unit) {
              case DAYS -> "d";
              case HOURS -> "h";
              case MINUTES -> "m";
              case SECONDS -> "s";
              default -> "";
            });
  }
}
