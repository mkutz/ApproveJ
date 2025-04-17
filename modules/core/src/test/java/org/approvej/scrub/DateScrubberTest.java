package org.approvej.scrub;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static org.approvej.scrub.Scrubbers.dates;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DateScrubberTest {

  @Test
  void apply() {
    RegexScrubber scrubber = dates(ISO_LOCAL_DATE);
    assertThat(scrubber.apply("datetime: 2023-10-01T12:00:00.000000"))
        .isEqualTo("datetime: [date 1]T12:00:00.000000");
  }
}
