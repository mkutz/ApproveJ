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
import org.jspecify.annotations.NullMarked;

@NullMarked
record RelativeDateTimeReplacementRecord(
    DateTimeFormatter dateTimeFormatter, Duration roundingDuration)
    implements RelativeDateTimeReplacement {

  /**
   * Creates and returns a copy of this with the given dateTimeFormatter.
   *
   * @param dateTimeFormatter the {@link DateTimeFormatter} to be used
   * @return a copy of this with the given dateTimeFormatter
   */
  @Override
  public RelativeDateTimeReplacement dateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
    return new RelativeDateTimeReplacementRecord(dateTimeFormatter, roundingDuration);
  }

  /**
   * Rounds the relative date/time to the given roundingDuration.
   *
   * @param roundingDuration a {@link Duration} to round to
   * @return a copy of this with the {@link #roundingDuration}
   */
  @Override
  public RelativeDateTimeReplacement roundedTo(Duration roundingDuration) {
    return new RelativeDateTimeReplacementRecord(dateTimeFormatter, roundingDuration);
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
