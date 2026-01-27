package org.approvej.json.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import org.approvej.configuration.Registry;
import org.approvej.print.PrintFormat;
import org.junit.jupiter.api.Test;

class JsonPrintFormatProviderTest {

  @Test
  void registry_resolve() {
    PrintFormat<?> format = Registry.resolve("json", PrintFormat.class);

    assertThat(format).isInstanceOf(JsonPrintFormat.class);
  }

  @Test
  void alias() {
    JsonPrintFormat<?> format = new JsonPrintFormat<>();

    assertThat(format.alias()).isEqualTo("json");
  }

  @Test
  void create() {
    JsonPrintFormat<?> format = new JsonPrintFormat<>();

    PrintFormat<?> created1 = format.create();
    PrintFormat<?> created2 = format.create();

    assertThat(created1).isNotSameAs(created2);
  }
}
