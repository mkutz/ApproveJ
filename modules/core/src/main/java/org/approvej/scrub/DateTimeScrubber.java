package org.approvej.scrub;

import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/**
 * Scrubs a {@link String} of date/time strings described by a {@link DateTimeFormatter} pattern
 * like "yyyy-MM-dd" for local dates using the wrapped {@link RegexScrubber}.
 */
public interface DateTimeScrubber extends Scrubber<String> {

  /**
   * Set the {@link Replacement} to be used.
   *
   * @param replacement a {@link Replacement} function
   * @return a copy of this using the given replacement
   */
  DateTimeScrubber replacement(Replacement replacement);

  /**
   * Set the replacement {@link Function} always returning the given staticReplacement.
   *
   * @param staticReplacement the static replacement {@link String}
   * @return a copy of this using the given string as replacement
   */
  DateTimeScrubber replacement(String staticReplacement);

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
