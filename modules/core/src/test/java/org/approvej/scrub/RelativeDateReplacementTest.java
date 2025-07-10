package org.approvej.scrub;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_ORDINAL_DATE;
import static org.approvej.scrub.Replacements.relativeDate;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RelativeDateReplacementTest {

  public static final LocalDate TODAY = LocalDate.now();

  @Test
  void apply_today() {
    DateTimeFormatter format = ISO_LOCAL_DATE;
    String unscrubbedValue = TODAY.format(format);
    assertEquals("[today]", relativeDate(format).apply(unscrubbedValue, 1));
  }

  @Test
  void apply_yesterday() {
    DateTimeFormatter format = ISO_LOCAL_DATE;
    String unscrubbedValue = TODAY.minusDays(1).format(format);
    assertEquals("[yesterday]", relativeDate(format).apply(unscrubbedValue, 1));
  }

  @Test
  void apply_tomorrow() {
    DateTimeFormatter format = ISO_LOCAL_DATE;
    String unscrubbedValue = TODAY.plusDays(1).format(format);
    assertEquals("[tomorrow]", relativeDate(format).apply(unscrubbedValue, 1));
  }

  @ParameterizedTest
  @ValueSource(ints = {2, 10, 10000})
  void apply_past(int daysAgo) {
    DateTimeFormatter format = ISO_LOCAL_DATE;
    String unscrubbedValue = TODAY.minusDays(daysAgo).format(format);
    assertEquals("[" + daysAgo + " days ago]", relativeDate(format).apply(unscrubbedValue, 1));
  }

  @ParameterizedTest
  @ValueSource(ints = {2, 10, 10000})
  void apply_future(int daysAhead) {
    DateTimeFormatter format = ISO_LOCAL_DATE;
    String unscrubbedValue = TODAY.plusDays(daysAhead).format(format);
    assertEquals(
        "[" + daysAhead + " days from now]", relativeDate(format).apply(unscrubbedValue, 1));
  }

  @Test
  void apply_basic_format() {
    DateTimeFormatter format = BASIC_ISO_DATE;
    String unscrubbedValue = TODAY.format(format);
    assertEquals("[today]", relativeDate(format).apply(unscrubbedValue, 1));
  }

  @Test
  void apply_ISO_ordinal_format() {
    DateTimeFormatter format = ISO_ORDINAL_DATE;
    String unscrubbedValue = TODAY.format(format);
    assertEquals("[today]", relativeDate(format).apply(unscrubbedValue, 1));
  }

  @Test
  void apply_custom_format() {
    DateTimeFormatter format = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
    String unscrubbedValue = TODAY.format(format);
    assertEquals("[today]", relativeDate(format).apply(unscrubbedValue, 1));
  }
}
