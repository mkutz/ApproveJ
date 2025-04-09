package org.approvej.scrub;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/** Scrubs a {@link String} by replacing all occurrences of a date time pattern. */
public class InstantScrubber extends RegexScrubber {

  private static final ZonedDateTime EXAMPLE_INSTANT = ZonedDateTime.now();

  /**
   * Creates {@link RegexScrubber} to replace date strings of the given pattern.
   *
   * @param dateFormatPattern a {@link DateTimeFormatter} to parse the dates
   * @return a new {@link InstantScrubber} with the given {@link DateTimeFormatter}.
   */
  public static InstantScrubber instants(DateTimeFormatter dateFormatPattern) {
    return new InstantScrubber(dateFormatPattern);
  }

  private InstantScrubber(DateTimeFormatter dateFormatPattern) {
    super(
        Pattern.compile(
            dateFormatPattern
                .format(EXAMPLE_INSTANT)
                .replaceAll("\\p{L}+", "\\\\p{L}+")
                .replaceAll("\\d", "\\\\d")),
        "[instant %d]"::formatted);
  }
}
