package org.approvej.print;

import static org.approvej.print.SingleLineStringPrintFormat.singleLineString;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SingleLineStringPrintFormatTest {

  @Test
  void constructor() {
    assertThat(new SingleLineStringPrintFormat()).isNotNull();
  }

  @Test
  void printer() {
    Printer<Object> printer = singleLineString().printer();

    assertThat(printer.apply("Hello")).isEqualTo("Hello");
    assertThat(printer.apply(123)).isEqualTo("123");
    assertThat(printer.apply(true)).isEqualTo("true");
    assertThat(printer.apply(null)).isEqualTo("null");
  }
}
