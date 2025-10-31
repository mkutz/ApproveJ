package org.approvej.yaml.jackson;

import static org.approvej.yaml.jackson.YamlPrintFormat.yaml;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class YamlPrintFormatTest {

  @Test
  void apply() {
    assertThat(yaml().apply(new Person("Micha", LocalDate.of(1982, 2, 19))))
        .isEqualTo(
            """
            ---
            name: "Micha"
            birthday: "1982-02-19"
            """);
  }

  @Test
  void apply_failure() {
    LocalDate someLocalDate = LocalDate.of(1982, 2, 19);
    YamlPrintFormat yamlPrintFormatWithoutJavaTime = yaml().using(new ObjectMapper());

    assertThatExceptionOfType(YamlPrinterException.class)
        .isThrownBy(() -> yamlPrintFormatWithoutJavaTime.apply(someLocalDate))
        .withMessage("Failed to print %s".formatted(someLocalDate));
  }

  @Test
  void using() {
    LocalDate someLocalDate = LocalDate.of(1982, 2, 19);
    YAMLMapper yamlMapper = YAMLMapper.builder().addModule(new JavaTimeModule()).build();

    assertThat(yaml().using(yamlMapper).apply(someLocalDate))
        .isEqualTo(
            """
            ---
            - 1982
            - 2
            - 19
            """);
  }

  @Test
  void filenameExtension() {
    assertThat(yaml().filenameExtension()).isEqualTo("yaml");
  }

  record Person(String name, LocalDate birthday) {}
}
