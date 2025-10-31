package org.approvej.json.jackson;

import static org.approvej.json.jackson.JsonPrettyPrinter.jsonPrettyPrinter;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

@Deprecated(since = "0.12", forRemoval = true)
class JsonPrettyPrinterTest {

  @Test
  void apply() {
    JsonPrettyPrinter<Object> jsonPrettyPrinter =
        jsonPrettyPrinter(JsonMapper.builder().addModule(new JavaTimeModule()).build());

    assertThat(jsonPrettyPrinter.apply(new Person("Micha", LocalDate.of(1982, 2, 19))))
        .isEqualTo(
            """
            {
              "name" : "Micha",
              "birthday" : "1982-02-19"
            }\
            """);
  }

  @Test
  void filenameExtension() {
    assertThat(jsonPrettyPrinter().filenameExtension()).isEqualTo("json");
  }

  record Person(String name, LocalDate birthday) {}
}
