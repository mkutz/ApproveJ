package org.approvej.yaml.jackson;

import static org.approvej.yaml.jackson.YamlPrinter.yamlPrinter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class YamlPrinterTest {

  @Test
  void constructor() {
    assertThat(yamlPrinter()).isNotNull();
    assertThat(yamlPrinter(new ObjectMapper())).isNotNull();
    assertThat(yamlPrinter(YAMLMapper.builder().build())).isNotNull();
  }

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

  @Test
  void apply_failure() {
    YamlPrinter<Object> yamlPrinterNoJavaTimeModule = yamlPrinter(new ObjectMapper());
    assertThatExceptionOfType(YamlPrinterException.class)
        .isThrownBy(
            () -> yamlPrinterNoJavaTimeModule.apply(new Person("Micha", LocalDate.of(1982, 2, 19))))
        .withCauseInstanceOf(InvalidDefinitionException.class);
  }

  @Test
  void filenameExtension() {
    assertThat(yamlPrinter().filenameExtension()).isEqualTo("yaml");
  }

  record Person(String name, LocalDate birthday) {}
}
