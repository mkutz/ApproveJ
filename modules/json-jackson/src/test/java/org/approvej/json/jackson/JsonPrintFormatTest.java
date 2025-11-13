package org.approvej.json.jackson;

import static org.approvej.json.jackson.JsonPrintFormat.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.fasterxml.jackson.databind.json.JsonMapper;
import java.time.LocalDate;
import org.approvej.print.Printer;
import org.junit.jupiter.api.Test;

class JsonPrintFormatTest {

  @Test
  void constructors() {
    assertThat(new JsonPrintFormat<>()).isNotNull();
  }

  @Test
  void initializers() {
    assertThat(json()).isNotNull();
    assertThat(json(JsonMapper.builder().build())).isNotNull();
  }

  @Test
  void printer() {
    assertThat(json().printer().apply(new Person("Micha", LocalDate.of(1982, 2, 19))))
        .isEqualTo(
            """
            {
              "name" : "Micha",
              "birthday" : "1982-02-19"
            }\
            """);
  }

  @Test
  void printer_json_string() {
    assertThat(json().printer().apply("{\"name\":\"Micha\",\"birthday\":\"1982-02-19\"}"))
        .isEqualTo(
            """
            {
              "name" : "Micha",
              "birthday" : "1982-02-19"
            }\
            """);
  }

  @Test
  void printer_invalid() {
    Printer<Object> printer = json().printer();
    assertThatExceptionOfType(JsonPrinterException.class).isThrownBy(() -> printer.apply("{"));
  }

  @Test
  void filenameExtension() {
    assertThat(json().filenameExtension()).isEqualTo("json");
  }

  record Person(String name, LocalDate birthday) {}
}
