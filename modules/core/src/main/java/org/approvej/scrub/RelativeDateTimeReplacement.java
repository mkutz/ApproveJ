package org.approvej.scrub;

import static java.lang.Math.abs;
import static java.util.stream.Collectors.joining;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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

  public RelativeDateTimeReplacement roundToWhole(ChronoUnit roundToUnit) {
    return new RelativeDateTimeReplacement(dateTimeFormatter, roundToUnit);
  }

  @Override
  public String apply(String match, Integer count) {
    ZonedDateTime parsed = dateTimeFormatter.parse(match, ZonedDateTime::from);
    Duration rounded = roundToUnit(Duration.between(ZonedDateTime.now(), parsed), roundToUnit);
    Duration absolute = rounded.abs();

    if (absolute.isZero()) {
      return "[now]";
    }

    Stream.Builder<String> parts = Stream.builder();

    if (rounded.isPositive()) {
      parts.add("in");
    }

    int seconds = absolute.toSecondsPart();
    int minutes = absolute.toMinutesPart();
    int hours = absolute.toHoursPart();
    long days = abs(rounded.toDaysPart());

    if (days != 0) {
      parts.add("%d%s".formatted(days, "d"));
    }

    if (hours != 0) {
      parts.add("%d%s".formatted(hours, "h"));
    }

    if (minutes != 0) {
      parts.add("%d%s".formatted(minutes, "m"));
    }

    if (seconds != 0) {
      parts.add("%d%s".formatted(seconds, "s"));
    }

    if (rounded.isNegative()) {
      parts.add("ago");
    }

    return "[%s]".formatted(parts.build().collect(joining(" ")));
  }

  private static Duration roundToUnit(Duration duration, ChronoUnit unit) {
    Duration roundedAbsolute =
        duration.abs().plus(unit.getDuration().dividedBy(2)).truncatedTo(unit);
    if (duration.isNegative()) {
      return roundedAbsolute.negated();
    }
    return roundedAbsolute;
  }
}
