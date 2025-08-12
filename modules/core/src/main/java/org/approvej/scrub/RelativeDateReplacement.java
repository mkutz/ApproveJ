package org.approvej.scrub;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
    long days = Duration.between(LocalDate.now().atStartOfDay(), parsed.atStartOfDay()).toDays();
    if (days == 0) return "[today]";
    else if (days == 1) return "[tomorrow]";
    else if (days == -1) return "[yesterday]";
    else if (days > 1) return "[%d days from now]".formatted(days);
    else return "[%d days ago]".formatted(-days);
  }
}
