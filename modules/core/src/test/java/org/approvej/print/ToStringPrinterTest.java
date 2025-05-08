package org.approvej.print;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ToStringPrinterTest {

  @Test
  void apply() {
    ToStringPrinter printer = new ToStringPrinter();
    assertThat(printer.apply("Hello")).isEqualTo("Hello");
    assertThat(printer.apply(123)).isEqualTo("123");
    assertThat(printer.apply(true)).isEqualTo("true");
    assertThat(printer.apply(null)).isEqualTo("null");
  }
}
