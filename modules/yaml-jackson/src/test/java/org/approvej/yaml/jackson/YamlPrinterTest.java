package org.approvej.yaml.jackson;

import static org.approvej.yaml.jackson.YamlPrinter.yamlPrinter;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class YamlPrinterTest {

  @Test
  void apply() {
    assertThat(yamlPrinter().apply(new Person("Micha", LocalDate.of(1982, 2, 19))))
        .isEqualTo(
            """
            ---
            name: "Micha"
            birthday: "1982-02-19"
            """);
  }

  record Person(String name, LocalDate birthday) {}
}
