package org.approvej.scrub;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class InstantScrubberTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "2015-04-26T12:59:48Z",
        "2015-04-26T12:59:48.900Z",
        "2015-04-26T09:59:48.987Z",
        "2015-04-26T12:59:48.987654Z",
        "2015-04-26T12:59:48.987654321Z"
      })
  void instants(String instantString) {
    RegexScrubber scrubber = Scrubbers.instants();
    assertThat(scrubber.apply("datetime: %s".formatted(instantString)))
        .isEqualTo("datetime: [instant 1]");
  }

  @Test
  void dateTimeFormat() {
    ZonedDateTime dateTime = ZonedDateTime.of(-12345, 4, 26, 12, 59, 48, 987_654_321, UTC);
    String pattern = "u";

    String scrubbed =
        Scrubbers.dateTimeFormat(pattern)
            .apply("datetime: %s".formatted(DateTimeFormatter.ofPattern(pattern).format(dateTime)));

    assertThat(scrubbed).isEqualTo("datetime: [datetime 1]");
  }

  @Test
  void dateTimeFormat_simple_date() {
    ZonedDateTime dateTime = ZonedDateTime.of(2015, 4, 26, 12, 59, 48, 987_654_321, UTC);
    String pattern = "yyyy-MM-dd";

    String scrubbed =
        Scrubbers.dateTimeFormat(pattern)
            .apply("datetime: %s".formatted(DateTimeFormatter.ofPattern(pattern).format(dateTime)));

    assertThat(scrubbed).isEqualTo("datetime: [datetime 1]");
  }

  @Test
  void dateTimeFormat_iso_instant() {
    ZonedDateTime dateTime = ZonedDateTime.of(2015, 4, 26, 12, 59, 48, 987_654_321, UTC);
    String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

    String scrubbed =
        Scrubbers.dateTimeFormat(pattern)
            .apply("datetime: %s".formatted(DateTimeFormatter.ofPattern(pattern).format(dateTime)));

    assertThat(scrubbed).isEqualTo("datetime: [datetime 1]");
  }
}
