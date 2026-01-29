package org.approvej.yaml.jackson3;

import static org.approvej.yaml.jackson3.YamlPrintFormat.yaml;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import tools.jackson.dataformat.yaml.YAMLMapper;

class YamlPrintFormatTest {

  @Test
  void constructors() {
    assertThat(new YamlPrintFormat<>()).isNotNull();
  }

  @Test
  void initializers() {
    assertThat(yaml()).isNotNull();
    assertThat(yaml(YAMLMapper.builder().build().writer())).isNotNull();
    assertThat(yaml(YAMLMapper.builder().build())).isNotNull();
  }

  @Test
  void printer() {
    assertThat(yaml().printer().apply(new Person("Micha", LocalDate.of(1982, 2, 19))))
        .isEqualTo(
            """
            ---
            name: "Micha"
            birthday: "1982-02-19"
            """);
  }

  @Test
  void yamlPrinterException() {
    // Verify YamlPrinterException exists and extends RuntimeException
    assertThat(YamlPrinterException.class).isAssignableTo(RuntimeException.class);
  }

  @Test
  void filenameExtension() {
    assertThat(yaml().filenameExtension()).isEqualTo("yaml");
  }

  record Person(String name, LocalDate birthday) {}
}
