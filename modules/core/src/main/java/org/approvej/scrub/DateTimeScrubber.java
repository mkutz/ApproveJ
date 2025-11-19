package org.approvej.scrub;

import java.time.format.DateTimeFormatter;

/**
 * Scrubs a {@link String} of date/time strings described by a {@link DateTimeFormatter} pattern
 * like "yyyy-MM-dd" for local dates using the wrapped {@link StringScrubber}.
 */
public interface DateTimeScrubber extends Scrubber<DateTimeScrubber, String, String> {

  /**
   * Set the {@link RelativeDateTimeReplacement} to be used. Automatically sets the {@link
   * RelativeDateTimeReplacement#dateTimeFormatter(DateTimeFormatter)} to the one of this.
   *
   * @param replacement a {@link RelativeDateTimeReplacement}
   * @return a copy of this using the given replacement
   */
  DateTimeScrubber replacement(RelativeDateTimeReplacement replacement);

  /**
   * Set the {@link RelativeDateReplacement} to be used. Automatically sets the {@link
   * RelativeDateReplacement#dateTimeFormatter(DateTimeFormatter)} to the one of this.
   *
   * @param replacement a {@link RelativeDateReplacement}
   * @return a copy of this using the given replacement
   */
  DateTimeScrubber replacement(RelativeDateReplacement replacement);
}
