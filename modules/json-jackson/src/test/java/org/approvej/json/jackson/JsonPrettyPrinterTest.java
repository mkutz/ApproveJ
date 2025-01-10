package org.approvej.json.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class JsonPrettyPrinterTest {

  @Test
  void test() {
    JsonMapper jsonMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
    var jsonPrettyPrinter = new JsonPrettyPrinter<>(jsonMapper);

    assertThat(jsonPrettyPrinter.apply(new Person("Micha", LocalDate.of(1982, 2, 19))))
        .isEqualTo(
            """
            {
              "name" : "Micha",
              "birthday" : [ 1982, 2, 19 ]
            }""");
  }

  record Person(String name, LocalDate birthday) {}
}
