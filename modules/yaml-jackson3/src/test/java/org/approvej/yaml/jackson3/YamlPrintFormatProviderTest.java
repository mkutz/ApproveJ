package org.approvej.yaml.jackson3;

import static org.assertj.core.api.Assertions.assertThat;

import org.approvej.configuration.Registry;
import org.approvej.print.PrintFormat;
import org.junit.jupiter.api.Test;

class YamlPrintFormatProviderTest {

  @Test
  void registry_resolve() {
    PrintFormat<?> format = Registry.resolve("yaml", PrintFormat.class);

    assertThat(format).isInstanceOf(YamlPrintFormat.class);
  }

  @Test
  void alias() {
    YamlPrintFormat<?> format = new YamlPrintFormat<>();

    assertThat(format.alias()).isEqualTo("yaml");
  }

  @Test
  void create() {
    YamlPrintFormat<?> format = new YamlPrintFormat<>();

    PrintFormat<?> created1 = format.create();
    PrintFormat<?> created2 = format.create();

    assertThat(created1).isNotSameAs(created2);
  }
}
