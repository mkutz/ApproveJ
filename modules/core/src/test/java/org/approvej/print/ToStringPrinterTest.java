package org.approvej.print;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ToStringPrinterTest {

  @Test
  void apply_string() {
    ToStringPrinter<String> printer = new ToStringPrinter<>();
    String exampleString = "Some string";

    assertThat(printer.apply(exampleString)).isEqualTo(exampleString);
  }

  @Test
  void apply_object() {
    ToStringPrinter<Object> printer = new ToStringPrinter<>();
    Object exampleObject = new Object();

    assertThat(printer.apply(exampleObject)).isEqualTo(exampleObject.toString());
  }
}
