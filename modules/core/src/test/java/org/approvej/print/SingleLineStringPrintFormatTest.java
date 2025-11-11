package org.approvej.print;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SingleLineStringPrintFormatTest {

  private final SingleLineStringPrintFormat format = SingleLineStringPrintFormat.singleLineString();

  @Test
  void printer() {
    assertThat(format.printer().apply("Hello")).isEqualTo("Hello");
    assertThat(format.printer().apply(123)).isEqualTo("123");
    assertThat(format.printer().apply(true)).isEqualTo("true");
    assertThat(format.printer().apply(null)).isEqualTo("null");
  }
}
