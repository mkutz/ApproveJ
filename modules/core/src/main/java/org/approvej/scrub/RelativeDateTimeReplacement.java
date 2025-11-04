package org.approvej.scrub;

import static java.lang.Math.abs;
import static java.util.stream.Collectors.joining;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

/**
 * Replaces each match of the given dateTimePattern (as defined by {@link DateTimeFormatter}) with a
 * relative description, like {@code [10m ago]}, {@code [in 2d 1h 3m 33s]}, {@code [13d ago]}.
 *
 * <p>The value is rounded to full seconds.
 */
public class RelativeDateTimeReplacement implements Replacement {

  private static final Duration PRECISION = Duration.ofMillis(500);
  private final DateTimeFormatter dateTimeFormatter;

  RelativeDateTimeReplacement(DateTimeFormatter dateTimeFormatter) {
    this.dateTimeFormatter = dateTimeFormatter;
  }

  @Override
  public String apply(String match, Integer count) {
    ZonedDateTime parsed = dateTimeFormatter.parse(match, ZonedDateTime::from);
    ZonedDateTime now = ZonedDateTime.now();
    Duration duration = Duration.between(now, parsed);
    Duration absolute = duration.abs();

    if (absolute.minus(PRECISION).isNegative()) {
      return "[now]";
    }

    Stream.Builder<String> parts = Stream.builder();

    if (duration.isPositive()) {
      parts.add("in");
    }

    long days = abs(duration.toDays());
    if (days != 0) {
      parts.add("%d%s".formatted(days, "d"));
    }

    int hours = absolute.toHoursPart();
    if (hours != 0) {
      parts.add("%d%s".formatted(hours, "h"));
    }

    int minutes = absolute.toMinutesPart();
    if (minutes != 0) {
      parts.add("%d%s".formatted(minutes, "m"));
    }

    int nanos = absolute.toNanosPart();
    int millis = absolute.toMillisPart() + (nanos >= 500_000_000 ? 1 : 0);
    int seconds = absolute.toSecondsPart() + (millis >= 500 ? 1 : 0);
    if (seconds != 0) {
      parts.add("%d%s".formatted(seconds, "s"));
    }

    if (duration.isNegative()) {
      parts.add("ago");
    }

    return "[%s]".formatted(parts.build().collect(joining(" ")));
  }
}
