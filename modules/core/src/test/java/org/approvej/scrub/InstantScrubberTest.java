package org.approvej.scrub;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class InstantScrubberTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
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

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "yyyy-MM-dd",
        "yy-M-d",
        "y-M-d",
        "y-D",
        "y-DD",
        "y-DDD",
        "HH:mm:ss.SSS",
        "H:m:s.S",
        "H:m:sX",
        "H:m:sXX",
        "H:m:sXXX",
        "H:m:sXXXX",
        "H:m:sZ",
        "H:m:sZZ",
        "H:m:sZZZ",
        "H:m:sZZZZ",
        "yyyy-MM-dd'T'HH:mm:ss.SSSX"
      })
  void dateTimeFormat(String pattern) {
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
    ZonedDateTime nonUtc =
        ZonedDateTime.of(2025, 9, 22, 23, 59, 48, 987_654_321, ZoneOffset.ofHours(-5));
    ZonedDateTime utc = ZonedDateTime.of(2026, 10, 12, 13, 39, 28, 987_654, UTC);
    ZonedDateTime bc = ZonedDateTime.of(-345, 11, 2, 3, 9, 8, 987, UTC);
    String unscrubbed =
        """
        non-utc: %s
        utc: %s
        bc: %s"""
            .formatted(
                dateTimeFormatter.format(nonUtc),
                dateTimeFormatter.format(utc),
                dateTimeFormatter.format(bc));

    String scrubbed = Scrubbers.dateTimeFormat(pattern).apply(unscrubbed);

    assertThat(scrubbed)
        .isEqualTo(
            """
            non-utc: [datetime 1]
            utc: [datetime 2]
            bc: [datetime 3]""")
        .as("unscrubbed was %n%s".formatted(unscrubbed));
  }
}
