package org.approvej.json.jackson;

import static org.approvej.json.jackson.JsonPrintFormat.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class JsonPrintFormatTest {

  @Test
  void apply_object() {
    assertThat(json().apply(new Person("Micha", LocalDate.of(1982, 2, 19))))
        .isEqualTo(
            """
            {
              "name" : "Micha",
              "birthday" : "1982-02-19"
            }\
            """);
  }

  @Test
  void apply_string() {
    assertThat(json().apply("{\"name\":\"Micha\",\"birthday\":\"1982-02-19\"}"))
        .isEqualTo(
            """
            {
              "name" : "Micha",
              "birthday" : "1982-02-19"
            }\
            """);
  }

  @Test
  void apply_string_invalid() {
    JsonPrintFormat jsonPrintFormat = json();
    assertThatExceptionOfType(JsonPrinterException.class)
        .isThrownBy(() -> jsonPrintFormat.apply("{"));
  }

  @Test
  void using() {
    assertThat(
            json()
                .using(JsonMapper.builder().addModule(new JavaTimeModule()).build())
                .apply(new Person("Micha", LocalDate.of(1982, 2, 19))))
        .isEqualTo(
            """
            {
              "name" : "Micha",
              "birthday" : [ 1982, 2, 19 ]
            }\
            """);
  }

  @Test
  void filenameExtension() {
    assertThat(json().filenameExtension()).isEqualTo("json");
  }

  record Person(String name, LocalDate birthday) {}
}
