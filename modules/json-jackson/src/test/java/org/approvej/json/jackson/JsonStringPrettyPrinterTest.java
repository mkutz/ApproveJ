package org.approvej.json.jackson;

import static org.approvej.json.jackson.JsonStringPrettyPrinter.jsonStringPrettyPrinter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

@Deprecated(since = "0.12", forRemoval = true)
class JsonStringPrettyPrinterTest {

  @Test
  void apply() {
    JsonStringPrettyPrinter jsonStringPrettyPrinter =
        jsonStringPrettyPrinter(JsonMapper.builder().addModule(new JavaTimeModule()).build());

    assertThat(jsonStringPrettyPrinter.apply("{\"name\":\"Micha\",\"birthday\":\"1982-02-19\"}"))
        .isEqualTo(
            """
            {
              "name" : "Micha",
              "birthday" : "1982-02-19"
            }\
            """);
  }

  @Test
  void apply_invalid() {
    JsonStringPrettyPrinter jsonStringPrettyPrinter =
        jsonStringPrettyPrinter(JsonMapper.builder().addModule(new JavaTimeModule()).build());

    assertThatExceptionOfType(JsonPrinterException.class)
        .isThrownBy(() -> jsonStringPrettyPrinter.apply("{"));
  }

  @Test
  void filenameExtension() {
    assertThat(jsonStringPrettyPrinter().filenameExtension()).isEqualTo("json");
  }
}
