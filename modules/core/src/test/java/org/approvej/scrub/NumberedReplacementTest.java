package org.approvej.scrub;

import static org.approvej.scrub.Replacements.numbered;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NumberedReplacementTest {

  @Test
  void apply() {
    assertThat(numbered().apply("John Doe", 1)).isEqualTo("[scrubbed 1]");
  }

  @Test
  void apply_label() {
    assertThat(numbered("name").apply("John Doe", 1)).isEqualTo("[name 1]");
  }
}
