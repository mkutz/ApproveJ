package org.approvej.scrub;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.approvej.scrub.Scrubbers.instants;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class InstantScrubberTest {

  @Test
  void apply() {
    RegexScrubber scrubber = instants(ISO_INSTANT).build();
    assertThat(scrubber.apply("datetime: %s".formatted(Instant.now())))
        .isEqualTo("datetime: [instant 1]");
  }
}
