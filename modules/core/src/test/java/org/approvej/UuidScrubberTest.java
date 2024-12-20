package org.approvej;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UuidScrubberTest {

  @Test
  void apply() {
    var unscrubbedValue =
        """
        {
          "id": "123e4567-e89b-12d3-a456-426614174000",
          "other-id": "123e4567-e89b-12d3-a456-426614174001",
          "same-id": "123e4567-e89b-12d3-a456-426614174000"
        }
        """;
    var expectedScrubbedValue =
        """
        {
          "id": "[uuid 1]",
          "other-id": "[uuid 2]",
          "same-id": "[uuid 1]"
        }
        """;

    assertThat(new UuidScrubber().apply(unscrubbedValue)).isEqualTo(expectedScrubbedValue);
  }
}
