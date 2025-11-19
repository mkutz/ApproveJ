package org.approvej.scrub;

import java.time.format.DateTimeFormatter;

/**
 * Replaces each match of the given dateTimePattern (as defined by {@link DateTimeFormatter}) with a
 * relative description, like {@code [today]}, {@code [yesterday]}, {@code [13 days from now]}.
 */
public interface RelativeDateReplacement extends Replacement<String> {

  /**
   * Creates and returns a copy of this with the given dateTimeFormatter.
   *
   * @param dateTimeFormatter the {@link DateTimeFormatter} to be used
   * @return a copy of this with the given dateTimeFormatter
   */
  RelativeDateReplacement dateTimeFormatter(DateTimeFormatter dateTimeFormatter);
}
