package org.approvej.yaml.jackson;

import static org.approvej.yaml.jackson.YamlPrintFormat.yaml;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

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
  void printer_failure() {
    YamlPrintFormat<Object> yamlPrinterNoJavaTimeModule = yaml(new ObjectMapper());
    LocalDate someLocalDate = LocalDate.of(1982, 2, 19);
    assertThatExceptionOfType(YamlPrinterException.class)
        .isThrownBy(() -> yamlPrinterNoJavaTimeModule.printer().apply(someLocalDate))
        .withMessage("Failed to print %s".formatted(someLocalDate));
  }

  @Test
  void filenameExtension() {
    assertThat(yaml().filenameExtension()).isEqualTo("yaml");
  }

  record Person(String name, LocalDate birthday) {}
}
