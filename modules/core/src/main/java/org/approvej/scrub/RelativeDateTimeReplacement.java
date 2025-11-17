package org.approvej.scrub;

import java.time.Duration;
import java.time.format.DateTimeFormatter;

/**
 * Replaces each match of the given dateTimePattern (as defined by {@link DateTimeFormatter}) with a
 * relative description, like {@code [10m ago]}, {@code [in 2d 1h 3m 33s]}, {@code [13d ago]}.
 *
 * <p>To avoid flaky results, the value is always rounded to a certain {@link Duration} (e.g. 1s).
 * This duration can be adjusted via {@link #roundedTo(Duration)}.
 */
public interface RelativeDateTimeReplacement extends Replacement {

  /**
   * Creates and returns a copy of this with the given {@link DateTimeFormatter}.
   *
   * @param dateTimeFormatter the {@link DateTimeFormatter} to be used
   * @return a copy of this using the given dateTimeFormatter
   */
  RelativeDateTimeReplacement dateTimeFormatter(DateTimeFormatter dateTimeFormatter);

  /**
   * Rounds the relative date/time to the given {@link Duration}.
   *
   * @param roundingDuration a {@link Duration} to round to
   * @return a copy of this using the given roundingDuration
   */
  RelativeDateTimeReplacement roundedTo(Duration roundingDuration);
}
