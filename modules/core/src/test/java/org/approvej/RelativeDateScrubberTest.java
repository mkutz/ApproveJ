package org.approvej;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_ORDINAL_DATE;
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
    var format = ISO_LOCAL_DATE;
    var unscrubbedValue = "It happens on %s at noon".formatted(TODAY.format(format));
    assertEquals(
        "It happens on [today] at noon", new RelativeDateScrubber(format).apply(unscrubbedValue));
  }

  @Test
  void apply_yesterday() {
    var format = ISO_LOCAL_DATE;
    var unscrubbedValue = "It happened on %s at noon".formatted(TODAY.minusDays(1).format(format));
    assertEquals(
        "It happened on [yesterday] at noon",
        new RelativeDateScrubber(format).apply(unscrubbedValue));
  }

  @Test
  void apply_tomorrow() {
    var format = ISO_LOCAL_DATE;
    var unscrubbedValue =
        "It will happen on %s at noon".formatted(TODAY.plusDays(1).format(format));
    assertEquals(
        "It will happen on [tomorrow] at noon",
        new RelativeDateScrubber(format).apply(unscrubbedValue));
  }

  @ParameterizedTest
  @ValueSource(ints = {2, 10, 10000})
  void apply_past(int daysAgo) {
    var format = ISO_LOCAL_DATE;
    var unscrubbedValue =
        "It happened on %s at noon".formatted(TODAY.minusDays(daysAgo).format(format));
    assertEquals(
        "It happened on [" + daysAgo + " days ago] at noon",
        new RelativeDateScrubber(format).apply(unscrubbedValue));
  }

  @ParameterizedTest
  @ValueSource(ints = {2, 10, 10000})
  void apply_future(int daysAhead) {
    var format = ISO_LOCAL_DATE;
    var unscrubbedValue =
        "It happened on %s at noon".formatted(TODAY.plusDays(daysAhead).format(format));
    assertEquals(
        "It happened on [" + daysAhead + " days from now] at noon",
        new RelativeDateScrubber(format).apply(unscrubbedValue));
  }

  @Test
  void apply_basic_format() {
    var format = BASIC_ISO_DATE;
    var unscrubbedValue = "It happens on %s".formatted(TODAY.format(format));
    assertEquals("It happens on [today]", new RelativeDateScrubber(format).apply(unscrubbedValue));
  }

  @Test
  void apply_ISO_ordinal_format() {
    var format = ISO_ORDINAL_DATE;
    var unscrubbedValue = "It happens on %s".formatted(TODAY.format(format));
    assertEquals("It happens on [today]", new RelativeDateScrubber(format).apply(unscrubbedValue));
  }

  @Test
  void apply_RFC1123_format() {
    var format = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
    var unscrubbedValue = "It happens on %s".formatted(TODAY.format(format));
    assertEquals("It happens on [today]", new RelativeDateScrubber(format).apply(unscrubbedValue));
  }
}
