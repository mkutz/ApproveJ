package org.approvej.scrub;

import static org.approvej.scrub.UuidScrubber.uuids;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UuidScrubberTest {

  private static final String EXAMPLE =
      """
      {
        "id": "123e4567-e89b-12d3-a456-426614174000",
        "other-id": "123e4567-e89b-12d3-a456-426614174001",
        "same-id": "123e4567-e89b-12d3-a456-426614174000"
      }
      """;

  @Test
  void apply() {
    assertThat(uuids().apply(EXAMPLE))
        .isEqualTo(
            """
            {
              "id": "[uuid 1]",
              "other-id": "[uuid 2]",
              "same-id": "[uuid 1]"
            }
            """);
  }

  @Test
  void apply_custom_replacement() {
    assertThat(uuids("<id%d>"::formatted).apply(EXAMPLE))
        .isEqualTo(
            """
            {
              "id": "<id1>",
              "other-id": "<id2>",
              "same-id": "<id1>"
            }
            """);
  }
}
