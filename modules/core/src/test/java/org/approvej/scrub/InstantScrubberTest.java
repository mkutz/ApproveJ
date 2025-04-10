package org.approvej.scrub;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.approvej.scrub.InstantScrubber.instants;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class InstantScrubberTest {

  @Test
  void apply() {
    var scrubber = instants(ISO_INSTANT);
    assertThat(scrubber.apply("datetime: %s".formatted(Instant.now())))
        .isEqualTo("datetime: [instant 1]");
  }
}
