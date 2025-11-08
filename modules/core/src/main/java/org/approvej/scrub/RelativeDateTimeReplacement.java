package org.approvej.scrub;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.stream.Collectors.joining;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

/**
 * Replaces each match of the given dateTimePattern (as defined by {@link DateTimeFormatter}) with a
 * relative description, like {@code [10m ago]}, {@code [in 2d 1h 3m 33s]}, {@code [13d ago]}.
 *
 * <p>The value is rounded to the {@link #roundingDuration}.
 */
public class RelativeDateTimeReplacement implements Replacement {

  private final DateTimeFormatter dateTimeFormatter;
  private final Duration roundingDuration;

  RelativeDateTimeReplacement(DateTimeFormatter dateTimeFormatter, Duration roundingDuration) {
    this.dateTimeFormatter = dateTimeFormatter;
    this.roundingDuration = roundingDuration;
  }

  /**
   * Creates and returns a copy of this with the given dateTimeFormatter.
   *
   * @param dateTimeFormatter the {@link DateTimeFormatter} to be used
   * @return a copy of this with the given dateTimeFormatter
   */
  public RelativeDateTimeReplacement dateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
    return new RelativeDateTimeReplacement(dateTimeFormatter, roundingDuration);
  }

  /**
   * Rounds the relative date/time to the given roundingDuration.
   *
   * @param roundingDuration a {@link Duration} to round to
   * @return a copy of this with the {@link #roundingDuration}
   */
  public RelativeDateTimeReplacement roundedTo(Duration roundingDuration) {
    return new RelativeDateTimeReplacement(dateTimeFormatter, roundingDuration);
  }

  @Override
  public String apply(String match, Integer count) {
    Instant parsed = dateTimeFormatter.parse(match, Instant::from);
    Duration rounded = roundToDuration(Duration.between(Instant.now(), parsed), roundingDuration);
    Duration absolute = rounded.abs();

    if (rounded.isZero()) {
      return "[now]";
    }

    return "[%s]"
        .formatted(
            Stream.of(
                    rounded.isPositive() ? "in" : "",
                    printPart(absolute.toDaysPart(), DAYS),
                    printPart(absolute.toHoursPart(), HOURS),
                    printPart(absolute.toMinutesPart(), MINUTES),
                    printPart(absolute.toSecondsPart(), SECONDS),
                    rounded.isNegative() ? "ago" : "")
                .filter(string -> !string.isBlank())
                .collect(joining(" ")));
  }

  private static Duration roundToDuration(Duration duration, Duration roundingDuration) {
    long unitMillis = roundingDuration.toMillis();
    return Duration.ofMillis(Math.round((double) duration.toMillis() / unitMillis) * unitMillis);
  }

  private static String printPart(long value, ChronoUnit unit) {
    if (value == 0) {
      return "";
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
