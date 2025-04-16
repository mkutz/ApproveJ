package org.approvej.scrub;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_ORDINAL_DATE;
import static org.approvej.scrub.Scrubbers.relativeDates;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RelativeDateScrubberTest {

  public static final LocalDate TODAY = LocalDate.now();

  @Test
  void apply_today() {
    DateTimeFormatter format = ISO_LOCAL_DATE;
    String unscrubbedValue = "It happens on %s at noon".formatted(TODAY.format(format));
    assertEquals("It happens on [today] at noon", relativeDates(format).apply(unscrubbedValue));
  }

  @Test
  void apply_yesterday() {
    DateTimeFormatter format = ISO_LOCAL_DATE;
    String unscrubbedValue =
        "It happened on %s at noon".formatted(TODAY.minusDays(1).format(format));
    assertEquals(
        "It happened on [yesterday] at noon", relativeDates(format).apply(unscrubbedValue));
  }

  @Test
  void apply_tomorrow() {
    DateTimeFormatter format = ISO_LOCAL_DATE;
    String unscrubbedValue =
        "It will happen on %s at noon".formatted(TODAY.plusDays(1).format(format));
    assertEquals(
        "It will happen on [tomorrow] at noon", relativeDates(format).apply(unscrubbedValue));
  }

  @ParameterizedTest
  @ValueSource(ints = {2, 10, 10000})
  void apply_past(int daysAgo) {
    DateTimeFormatter format = ISO_LOCAL_DATE;
    String unscrubbedValue =
        "It happened on %s at noon".formatted(TODAY.minusDays(daysAgo).format(format));
    assertEquals(
        "It happened on [" + daysAgo + " days ago] at noon",
        relativeDates(format).apply(unscrubbedValue));
  }

  @ParameterizedTest
  @ValueSource(ints = {2, 10, 10000})
  void apply_future(int daysAhead) {
    DateTimeFormatter format = ISO_LOCAL_DATE;
    String unscrubbedValue =
        "It happened on %s at noon".formatted(TODAY.plusDays(daysAhead).format(format));
    assertEquals(
        "It happened on [" + daysAhead + " days from now] at noon",
        relativeDates(format).apply(unscrubbedValue));
  }

  @Test
  void apply_basic_format() {
    DateTimeFormatter format = BASIC_ISO_DATE;
    String unscrubbedValue = "It happens on %s".formatted(TODAY.format(format));
    assertEquals("It happens on [today]", relativeDates(format).apply(unscrubbedValue));
  }

  @Test
  void apply_ISO_ordinal_format() {
    DateTimeFormatter format = ISO_ORDINAL_DATE;
    String unscrubbedValue = "It happens on %s".formatted(TODAY.format(format));
    assertEquals("It happens on [today]", relativeDates(format).apply(unscrubbedValue));
  }

  @Test
  void apply_RFC1123_format() {
    DateTimeFormatter format = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
    String unscrubbedValue = "It happens on %s".formatted(TODAY.format(format));
    assertEquals("It happens on [today]", relativeDates(format).apply(unscrubbedValue));
  }
}
