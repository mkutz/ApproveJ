package org.approvej.json.jackson;

import static java.util.UUID.randomUUID;
import static org.approvej.json.jackson.JsonPointerScrubber.jsonPointer;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.time.LocalDateTime;
import org.approvej.scrub.Scrubber;
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
    JsonMapper jsonMapper = JsonMapper.builder().build();
    JsonNode jsonNode = jsonMapper.readTree(EXAMPLE_JSON);
    Scrubber<JsonNode> idScrubber = jsonPointer("/id").build();
    JsonNode scrubbedJsonNode = idScrubber.apply(jsonNode);

    assertThat(scrubbedJsonNode.at("/id").textValue()).isEqualTo("[scrubbed]");
  }

  @Test
  void apply_custom_static_replacement() throws JsonProcessingException {
    JsonNode jsonNode = JSON_MAPPER.readTree(EXAMPLE_JSON);
    Scrubber<JsonNode> enrollmentDateScrubber =
        jsonPointer("/enrollmentDate").replacement("[scrubbed enrollment date]").build();

    JsonNode scrubbedJsonNode = enrollmentDateScrubber.apply(jsonNode);

    assertThat(scrubbedJsonNode.at("/enrollmentDate").textValue())
        .isEqualTo("[scrubbed enrollment date]");
  }

  @Test
  void apply_custom_replacement() throws JsonProcessingException {
    JsonNode jsonNode = JSON_MAPPER.readTree(EXAMPLE_JSON);
    Scrubber<JsonNode> enrollmentDateScrubber =
        jsonPointer("/enrollmentDate").replacement("<enrollmentDate>"::formatted).build();

    JsonNode scrubbedJsonNode = enrollmentDateScrubber.apply(jsonNode);

    assertThat(scrubbedJsonNode.at("/enrollmentDate").textValue()).isEqualTo("<enrollmentDate>");
  }
}
