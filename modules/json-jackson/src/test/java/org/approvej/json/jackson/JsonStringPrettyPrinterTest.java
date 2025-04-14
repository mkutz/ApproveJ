package org.approvej.json.jackson;

import static org.approvej.json.jackson.JsonStringPrettyPrinter.jsonStringPrettyPrinter;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

class JsonStringPrettyPrinterTest {

  @Test
  void apply() {
    var jsonMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    assertThat(
            jsonStringPrettyPrinter(jsonMapper)
                .apply("{\"name\":\"Micha\",\"birthday\":\"1982-02-19\"}"))
        .isEqualTo(
            """
            {
              "name" : "Micha",
              "birthday" : "1982-02-19"
            }""");
  }
}
