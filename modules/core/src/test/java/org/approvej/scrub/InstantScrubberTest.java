package org.approvej.scrub;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.approvej.scrub.InstantScrubber.instants;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InstantScrubberTest {

  @Test
  void apply() {
    var scrubber = instants(ISO_LOCAL_DATE_TIME);
    assertThat(scrubber.apply("datetime: 2023-10-01T12:00:00.000000"))
        .isEqualTo("datetime: [instant 1]");
  }
}
