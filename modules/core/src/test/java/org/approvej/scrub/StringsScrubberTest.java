package org.approvej.scrub;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class StringsScrubberTest {

  @Test
  void apply() {
    String randomId = randomUUID().toString();
    String randomTimestamp = Instant.now().toString();
    String template =
        """
        {
          "id": "%s",
          "timestamp": "%s",
          "content": "Static content"
        }
        """;

    String scrubbed =
        Scrubbers.strings(randomId, randomTimestamp)
            .apply(template.formatted(randomId, randomTimestamp));

    assertThat(scrubbed).isEqualTo(template.formatted("[scrubbed 1]", "[scrubbed 2]"));
  }
}
