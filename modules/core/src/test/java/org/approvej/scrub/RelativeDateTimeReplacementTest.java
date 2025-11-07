package org.approvej.scrub;

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.approvej.scrub.Replacements.relativeDateTime;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class RelativeDateTimeReplacementTest {

  public static final ZonedDateTime NOW = ZonedDateTime.now();

  @Test
  void apply_now() {
    String unscrubbedValue = NOW.format(ISO_ZONED_DATE_TIME);
    assertEquals(
        "[now]",
        relativeDateTime().dateTimeFormatter(ISO_ZONED_DATE_TIME).apply(unscrubbedValue, 1));
  }

  @Test
  void apply_10_minutes_ago() {
    String unscrubbedValue = NOW.minusMinutes(10).format(ISO_ZONED_DATE_TIME);
    assertEquals(
        "[10m ago]",
        relativeDateTime().dateTimeFormatter(ISO_ZONED_DATE_TIME).apply(unscrubbedValue, 1));
  }

  @Test
  void apply_in_10_minutes() {
    String unscrubbedValue = NOW.plusMinutes(10).format(ISO_ZONED_DATE_TIME);
    assertEquals(
        "[in 10m]",
        relativeDateTime().dateTimeFormatter(ISO_ZONED_DATE_TIME).apply(unscrubbedValue, 1));
  }

  @Test
  void apply_in_1_week_minus_seconds() {
    String unscrubbedValue = NOW.plusDays(7).minusSeconds(5).format(ISO_ZONED_DATE_TIME);
    assertEquals(
        "[in 6d 23h 59m 55s]",
        relativeDateTime().dateTimeFormatter(ISO_ZONED_DATE_TIME).apply(unscrubbedValue, 1));
  }

  @Test
  void roundedToWhole_days() {
    String unscrubbedValue = NOW.plusDays(7).minusSeconds(5).format(ISO_ZONED_DATE_TIME);
    assertEquals(
        "[in 7d]",
        relativeDateTime()
            .dateTimeFormatter(ISO_ZONED_DATE_TIME)
            .roundedTo(DAYS.getDuration())
            .apply(unscrubbedValue, 1));
  }

  @Test
  void roundedToWhole_hours() {
    String unscrubbedValue = NOW.plusDays(7).minusHours(1).format(ISO_ZONED_DATE_TIME);
    assertEquals(
        "[in 6d 23h]",
        relativeDateTime()
            .dateTimeFormatter(ISO_ZONED_DATE_TIME)
            .roundedTo(HOURS.getDuration())
            .apply(unscrubbedValue, 1));
  }

  @Test
  void roundedToWhole_minutes() {
    String unscrubbedValue =
        NOW.plusDays(7).minusHours(1).minusMinutes(5).minusSeconds(5).format(ISO_ZONED_DATE_TIME);
    assertEquals(
        "[in 6d 22h 55m]",
        relativeDateTime()
            .dateTimeFormatter(ISO_ZONED_DATE_TIME)
            .roundedTo(MINUTES.getDuration())
            .apply(unscrubbedValue, 1));
  }
}
