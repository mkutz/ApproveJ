package org.approvej.scrub;

import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.approvej.scrub.Scrubbers.instants;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class InstantScrubberTest {

  @Test
  void apply() {
    RegexScrubber scrubber = instants(ISO_INSTANT);
    assertThat(scrubber.apply("datetime: %s".formatted(Instant.now())))
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
        "yyyy-GGGG",
        "yyyy-GGG",
        "yyyy-GG",
        "yyyy-G",
        "yy-M-d",
        "y-M-d",
        "EEEE M d",
        "EEE M d",
        "EE M d",
        "E M d",
        "eeee M d",
        "ccc M d",
        "ee M d",
        "c M d",
        "yyyy-M",
        "yyyy-MM",
        "yyyy-MMM",
        "yyyy-MMMM",
        "yyyy-L",
        "yyyy-LL",
        "yyyy-LLL",
        "yyyy-LLLL",
        "YYYY-ww",
        "YY-w",
        "Y-w",
        "M-W",
        "M-F",
        "yyyy-QQQQ",
        "yyyy-QQQ",
        "yyyy-QQ",
        "yyyy-Q",
        "y-D",
        "y-DD",
        "y-DDD",
        "HH:mm:ss.SSS",
        "kk:mm",
        "k:m",
        "KK:mm",
        "K:m",
        "hh:mm",
        "h:m",
        "h:m a",
        "H:m:s.S",
        "H:m:s:n",
        "H:m:s:nnnnnnnnnn",
        "N",
        "H:m:sX",
        "H:m:sXX",
        "H:m:sXXX",
        "H:m:sXXXX",
        "H:mZ",
        "H:mZZ",
        "H:mZZZ",
        "H:mZZZZ",
        "H:mVV",
        "H:mx",
        "H:mxx",
        "H:mxxx",
        "H:mxxxx",
        "H:mO",
        "H:mOOOO",
        "yyyy-MM-dd'T'HH:mm:ss.SSSX"
      })
  void dateTimeFormat(String pattern) {
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern, Locale.US);
    ZonedDateTime nonUtc =
        ZonedDateTime.of(2025, 9, 22, 23, 59, 48, 987_654_321, ZoneOffset.ofHours(-5));
    ZonedDateTime utc = ZonedDateTime.of(2026, 10, 12, 13, 39, 28, 987_654, UTC);
    ZonedDateTime bc =
        ZonedDateTime.of(-345, 11, 2, 3, 9, 8, 987, ZoneOffset.ofHoursMinutes(13, 45));
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
