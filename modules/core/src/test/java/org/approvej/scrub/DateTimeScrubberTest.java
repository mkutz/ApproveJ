package org.approvej.scrub;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DateTimeScrubberTest {

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
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS[X]"
      })
  void dateTimeFormat(String pattern) {
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
    ZonedDateTime nonUtc =
        ZonedDateTime.of(2025, 9, 22, 23, 59, 48, 987_654_321, ZoneOffset.ofHours(-5));
    ZonedDateTime utc = ZonedDateTime.of(2026, 10, 12, 13, 39, 28, 987_654, UTC);
    ZonedDateTime bc =
        ZonedDateTime.of(-345, 11, 2, 3, 9, 8, 987, ZoneOffset.ofHoursMinutes(13, 45));
    String unscrubbed =
        """
        non-utc: %s
        utc: %s
        bc: %s\
        """
            .formatted(
                dateTimeFormatter.format(nonUtc),
                dateTimeFormatter.format(utc),
                dateTimeFormatter.format(bc));

    String scrubbed = Scrubbers.dateTimeFormat(pattern).apply(unscrubbed);

    assertThat(scrubbed)
        .as("unscrubbed was %n%s".formatted(unscrubbed))
        .isEqualTo(
            """
            non-utc: [datetime 1]
            utc: [datetime 2]
            bc: [datetime 3]\
            """);
  }

  @Test
  void isoLocalDates() {
    assertThat(
            Scrubbers.isoLocalDates()
                .apply(
                    "isoLocalDate: %s"
                        .formatted(DateTimeFormatter.ISO_LOCAL_DATE.format(ZonedDateTime.now()))))
        .isEqualTo("isoLocalDate: [isoLocalDate 1]");
  }

  @Test
  void isoOffsetDates() {
    assertThat(
            Scrubbers.isoOffsetDates()
                .apply(
                    "isoOffsetDate: %s"
                        .formatted(DateTimeFormatter.ISO_OFFSET_DATE.format(ZonedDateTime.now()))))
        .isEqualTo("isoOffsetDate: [isoOffsetDate 1]");
  }

  @Test
  void isoDates() {
    assertThat(
            Scrubbers.isoDates()
                .apply(
                    "isoDate: %s"
                        .formatted(DateTimeFormatter.ISO_DATE.format(ZonedDateTime.now()))))
        .isEqualTo("isoDate: [isoDate 1]");
  }

  @Test
  void isoLocalTimes() {
    assertThat(
            Scrubbers.isoLocalTimes()
                .apply(
                    "isoLocalTime: %s"
                        .formatted(DateTimeFormatter.ISO_LOCAL_TIME.format(ZonedDateTime.now()))))
        .isEqualTo("isoLocalTime: [isoLocalTime 1]");
  }

  @Test
  void isoOffsetTimes() {
    assertThat(
            Scrubbers.isoOffsetTimes()
                .apply(
                    "isoOffsetTime: %s"
                        .formatted(DateTimeFormatter.ISO_OFFSET_TIME.format(ZonedDateTime.now()))))
        .isEqualTo("isoOffsetTime: [isoOffsetTime 1]");
  }

  @Test
  void isoTimes() {
    assertThat(
            Scrubbers.isoTimes()
                .apply(
                    "isoTime: %s"
                        .formatted(DateTimeFormatter.ISO_TIME.format(ZonedDateTime.now()))))
        .isEqualTo("isoTime: [isoTime 1]");
  }

  @Test
  void isoLocalDateTimes() {
    assertThat(
            Scrubbers.isoLocalDateTimes()
                .apply(
                    "isoLocalDateTime: %s"
                        .formatted(
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(ZonedDateTime.now()))))
        .isEqualTo("isoLocalDateTime: [isoLocalDateTime 1]");
  }

  @Test
  void isoOffsetDateTimes() {
    assertThat(
            Scrubbers.isoOffsetDateTimes()
                .apply(
                    "isoOffsetDateTime: %s"
                        .formatted(
                            DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now()))))
        .isEqualTo("isoOffsetDateTime: [isoOffsetDateTime 1]");
  }

  @Test
  void isoZonedDateTimes() {
    DateTimeScrubber isoZonedDateTimes = Scrubbers.isoZonedDateTimes();
    assertThat(
            isoZonedDateTimes.apply(
                "isoZonedDateTime: %s"
                    .formatted(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(ZonedDateTime.now()))))
        .isEqualTo("isoZonedDateTime: [isoZonedDateTime 1]");
  }

  @Test
  void isoDateTimes() {
    assertThat(
            Scrubbers.isoDateTimes()
                .apply(
                    "isoDateTime: %s"
                        .formatted(DateTimeFormatter.ISO_DATE_TIME.format(ZonedDateTime.now()))))
        .isEqualTo("isoDateTime: [isoDateTime 1]");
  }

  @Test
  void isoOrdinalDates() {
    assertThat(
            Scrubbers.isoOrdinalDates()
                .apply(
                    "isoOrdinalDate: %s"
                        .formatted(DateTimeFormatter.ISO_ORDINAL_DATE.format(ZonedDateTime.now()))))
        .isEqualTo("isoOrdinalDate: [isoOrdinalDate 1]");
  }

  @Test
  void isoWeekDates() {
    assertThat(
            Scrubbers.isoWeekDates()
                .apply(
                    "isoWeekDate: %s"
                        .formatted(DateTimeFormatter.ISO_WEEK_DATE.format(ZonedDateTime.now()))))
        .isEqualTo("isoWeekDate: [isoWeekDate 1]");
  }

  @Test
  void isoInstants() {
    assertThat(
            Scrubbers.isoInstants()
                .apply(
                    "isoInstant: %s"
                        .formatted(DateTimeFormatter.ISO_INSTANT.format(ZonedDateTime.now()))))
        .isEqualTo("isoInstant: [isoInstant 1]");
  }

  @Test
  void basicIsoDates() {
    assertThat(
            Scrubbers.basicIsoDates()
                .apply(
                    "basicIsoDate: %s"
                        .formatted(DateTimeFormatter.BASIC_ISO_DATE.format(ZonedDateTime.now()))))
        .isEqualTo("basicIsoDate: [basicIsoDate 1]");
  }

  @Test
  void rfc1123DateTimes() {
    assertThat(
            Scrubbers.rfc1123DateTimes()
                .apply(
                    "rfc1123DateTime: %s"
                        .formatted(
                            DateTimeFormatter.RFC_1123_DATE_TIME.format(
                                ZonedDateTime.now().withZoneSameInstant(ZoneId.of("GMT"))))))
        .isEqualTo("rfc1123DateTime: [rfc1123DateTime 1]");
  }
}
