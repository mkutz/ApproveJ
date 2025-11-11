package org.approvej;

import static org.assertj.core.api.Assertions.assertThat;

import org.approvej.print.SingleLineStringPrintFormat;
import org.junit.jupiter.api.Test;

class ConfigurationTest {

  @Test
  void configuration() {
    assertThat(Configuration.configuration).isNotNull();
    assertThat(Configuration.configuration.defaultPrintFormat())
        .isInstanceOf(SingleLineStringPrintFormat.class);
  }
}
