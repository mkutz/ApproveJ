package org.approvej.scrub;

import static org.approvej.scrub.Replacements.labeled;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class LabelReplacementTest {

  @Test
  void apply() {
    assertThat(labeled("name").apply("John Doe", 1)).isEqualTo("[name]");
  }
}
