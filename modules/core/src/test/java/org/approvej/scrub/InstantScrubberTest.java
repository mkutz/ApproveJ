package org.approvej.scrub;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.approvej.scrub.InstantScrubber.instants;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InstantScrubberTest {

  @Test
  void apply() {
    var scrubber = instants(ISO_INSTANT);
    assertThat(scrubber.apply("datetime: 2025-04-10T11:48:04.459229Z"))
        .isEqualTo("datetime: [instant 1]");
  }
}
