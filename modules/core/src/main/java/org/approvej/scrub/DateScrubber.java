package org.approvej.scrub;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/** Scrubs a {@link String} by replacing all occurrences of a date pattern. */
public class DateScrubber extends RegexScrubber {

  private static final LocalDate EXAMPLE_DATE = LocalDate.of(4567, 12, 30);

  /**
   * Creates {@link RegexScrubber} to replace date strings of the given pattern.
   *
   * @param dateFormatPattern a {@link DateTimeFormatter} to parse the dates
   * @return a new {@link DateScrubber} with the given {@link DateTimeFormatter}.
   */
  public static DateScrubber dates(DateTimeFormatter dateFormatPattern) {
    return new DateScrubber(dateFormatPattern);
  }

  private DateScrubber(DateTimeFormatter dateFormatPattern) {
    super(
        Pattern.compile(
            dateFormatPattern
                .format(EXAMPLE_DATE)
                .replaceAll("\\p{L}+", "\\\\p{L}+")
                .replaceAll("\\d", "\\\\d")),
        "[date %d]"::formatted);
  }
}
