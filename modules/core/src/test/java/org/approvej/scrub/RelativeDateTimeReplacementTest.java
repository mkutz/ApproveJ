package org.approvej.scrub;

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static org.approvej.scrub.Replacements.relativeDateTime;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

class RelativeDateTimeReplacementTest {

  public static final ZonedDateTime NOW = ZonedDateTime.now();

  @Test
  void apply_now() {
    DateTimeFormatter format = ISO_ZONED_DATE_TIME;
    String unscrubbedValue = NOW.format(format);
    assertEquals("[now]", relativeDateTime(format).apply(unscrubbedValue, 1));
  }

  @Test
  void apply_10_minutes_ago() {
    DateTimeFormatter format = ISO_ZONED_DATE_TIME;
    String unscrubbedValue = NOW.minusMinutes(10).format(format);
    assertEquals("[10m ago]", relativeDateTime(format).apply(unscrubbedValue, 1));
  }

  @Test
  void apply_in_1_day_2_hours_3_minutes_4_seconds() {
    DateTimeFormatter format = ISO_ZONED_DATE_TIME;
    String unscrubbedValue =
        NOW.plusDays(1).plusHours(2).plusMinutes(3).plusSeconds(4).format(format);
    assertEquals("[in 1d 2h 3m 4s]", relativeDateTime(format).apply(unscrubbedValue, 1));
  }

  @Test
  void apply_1_day_2_hours_3_minutes_4_seconds_ago() {
    DateTimeFormatter format = ISO_ZONED_DATE_TIME;
    String unscrubbedValue =
        NOW.minusDays(1).minusHours(2).minusMinutes(3).minusSeconds(4).format(format);
    assertEquals("[1d 2h 3m 4s ago]", relativeDateTime(format).apply(unscrubbedValue, 1));
  }
}
