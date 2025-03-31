package org.approvej.json.jackson;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class JsonPointerScrubberTest {

  public static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();
  private static final String EXAMPLE_JSON =
      """
      {
        "id": "%s",
        "name": "John Doe",
        "birthday": "1990-01-01",
        "enrollmentDate": "%s"
      }
      """
          .formatted(randomUUID(), LocalDateTime.now());

  @Test
  void apply() throws JsonProcessingException {
    var jsonMapper = JsonMapper.builder().build();
    var jsonNode = jsonMapper.readTree(EXAMPLE_JSON);
    var idScrubber = new JsonPointerScrubber(JsonPointer.compile("/id"));
    var scrubbedJsonNode = idScrubber.apply(jsonNode);

    assertThat(scrubbedJsonNode.at("/id").textValue()).isEqualTo("[scrubbed]");
  }

  @Test
  void apply_custom_replacement() throws JsonProcessingException {
    var jsonNode = JSON_MAPPER.readTree(EXAMPLE_JSON);
    var enrollmentDateScrubber =
        new JsonPointerScrubber(
            JsonPointer.compile("/enrollmentDate"), "[scrubbed enrollment date]");

    var scrubbedJsonNode = enrollmentDateScrubber.apply(jsonNode);

    assertThat(scrubbedJsonNode.at("/enrollmentDate").textValue())
        .isEqualTo("[scrubbed enrollment date]");
  }
}
