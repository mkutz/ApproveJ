package org.approvej.scrub;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_ORDINAL_DATE;
import static org.approvej.scrub.Scrubbers.basicIsoDates;
import static org.approvej.scrub.Scrubbers.dateTimeFormat;
import static org.approvej.scrub.Scrubbers.isoLocalDates;
import static org.approvej.scrub.Scrubbers.isoOrdinalDates;
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
    String unscrubbedValue = "It happens on %s at noon".formatted(TODAY.format(ISO_LOCAL_DATE));
    assertEquals(
        "It happens on [today] at noon",
        isoLocalDates().replaceWithRelativeDate().apply(unscrubbedValue));
  }

  @Test
  void apply_yesterday() {
    String unscrubbedValue =
        "It happened on %s at noon".formatted(TODAY.minusDays(1).format(ISO_LOCAL_DATE));
    assertEquals(
        "It happened on [yesterday] at noon",
        isoLocalDates().replaceWithRelativeDate().apply(unscrubbedValue));
  }

  @Test
  void apply_tomorrow() {
    String unscrubbedValue =
        "It will happen on %s at noon".formatted(TODAY.plusDays(1).format(ISO_LOCAL_DATE));
    assertEquals(
        "It will happen on [tomorrow] at noon",
        isoLocalDates().replaceWithRelativeDate().apply(unscrubbedValue));
  }

  @ParameterizedTest
  @ValueSource(ints = {2, 10, 27})
  void apply_past(int daysAgo) {
    String unscrubbedValue =
        "It happened on %s at noon".formatted(TODAY.minusDays(daysAgo).format(ISO_LOCAL_DATE));
    assertEquals(
        "It happened on [" + daysAgo + " days ago] at noon",
        isoLocalDates().replaceWithRelativeDate().apply(unscrubbedValue));
  }

  @ParameterizedTest
  @ValueSource(ints = {2, 10, 27})
  void apply_future(int daysAhead) {
    String unscrubbedValue =
        "It happened on %s at noon".formatted(TODAY.plusDays(daysAhead).format(ISO_LOCAL_DATE));
    assertEquals(
        "It happened on [in " + daysAhead + " days] at noon",
        isoLocalDates().replaceWithRelativeDate().apply(unscrubbedValue));
  }

  @Test
  void apply_basic_format() {
    String unscrubbedValue = "It happens on %s".formatted(TODAY.format(BASIC_ISO_DATE));
    assertEquals(
        "It happens on [today]", basicIsoDates().replaceWithRelativeDate().apply(unscrubbedValue));
  }

  @Test
  void apply_ISO_ordinal_format() {
    String unscrubbedValue = "It happens on %s".formatted(TODAY.format(ISO_ORDINAL_DATE));
    assertEquals(
        "It happens on [today]",
        isoOrdinalDates().replaceWithRelativeDate().apply(unscrubbedValue));
  }

  @Test
  void apply_custom_format() {
    String dateTimePattern = "EEEE, dd MMMM yyyy";
    String unscrubbedValue =
        "It happens on %s".formatted(TODAY.format(DateTimeFormatter.ofPattern(dateTimePattern)));
    assertEquals(
        "It happens on [today]",
        dateTimeFormat(dateTimePattern).replaceWithRelativeDate().apply(unscrubbedValue));
  }
}
