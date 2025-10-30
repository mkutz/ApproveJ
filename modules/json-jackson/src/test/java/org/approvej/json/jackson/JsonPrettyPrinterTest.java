package org.approvej.json.jackson;

import static org.approvej.json.jackson.JsonPrettyPrinter.json;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class JsonPrettyPrinterTest {

  @Test
  void apply() {
    JsonMapper jsonMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
    JsonPrettyPrinter<Object> jsonPrettyPrinter = json(jsonMapper);

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
    assertThat(json().filenameExtension()).isEqualTo("json");
  }

  record Person(String name, LocalDate birthday) {}
}
