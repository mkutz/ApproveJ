package org.approvej.scrub;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.approvej.scrub.Scrubbers.dateTimeFormat;
import static org.approvej.scrub.Scrubbers.instants;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
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

  @ParameterizedTest
  @ValueSource(strings = {"G", "GG", "GGGG"})
  void apply_era(String pattern) {
    RegexScrubber scrubber = dateTimeFormat(pattern);
    assertThat(scrubber.apply("G: %s".formatted("AD"))).isEqualTo("G: [instant 1]");
  }
}
