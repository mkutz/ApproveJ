package org.approvej;

import static org.assertj.core.api.Assertions.assertThat;

import org.approvej.print.ToStringPrinter;
import org.junit.jupiter.api.Test;

class ConfigurationTest {

  @Test
  void testLoadConfiguration() {
    assertThat(Configuration.configuration).isNotNull();
    assertThat(Configuration.configuration.defaultPrinter()).isInstanceOf(ToStringPrinter.class);
  }
}
