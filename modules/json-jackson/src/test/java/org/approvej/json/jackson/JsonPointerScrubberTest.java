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
        "enrollmentDate": "%s",
        "items": [
          { "itemId": "%s" }
        ]
      }
      """
          .formatted(randomUUID(), LocalDateTime.now(), randomUUID());

  @Test
  void apply() throws JsonProcessingException {
    JsonMapper jsonMapper = JsonMapper.builder().build();
    JsonNode jsonNode = jsonMapper.readTree(EXAMPLE_JSON);
    Scrubber<JsonNode> idScrubber = jsonPointer("/id");
    JsonNode scrubbedJsonNode = idScrubber.apply(jsonNode);

    assertThat(scrubbedJsonNode.at("/id").textValue()).isEqualTo("[scrubbed]");
  }

  @Test
  void apply_deeper_value() throws JsonProcessingException {
    JsonMapper jsonMapper = JsonMapper.builder().build();
    JsonNode jsonNode = jsonMapper.readTree(EXAMPLE_JSON);
    Scrubber<JsonNode> itemScrubber = jsonPointer("/items/0/itemId");
    JsonNode scrubbedJsonNode = itemScrubber.apply(jsonNode);

    assertThat(scrubbedJsonNode.at("/items/0/itemId").textValue()).isEqualTo("[scrubbed]");
  }

  @Test
  void apply_subtree() throws JsonProcessingException {
    JsonMapper jsonMapper = JsonMapper.builder().build();
    JsonNode jsonNode = jsonMapper.readTree(EXAMPLE_JSON);
    Scrubber<JsonNode> itemScrubber = jsonPointer("/items/0");
    JsonNode scrubbedJsonNode = itemScrubber.apply(jsonNode);

    assertThat(scrubbedJsonNode.at("/items/0").textValue()).isEqualTo("[scrubbed]");
  }

  @Test
  void apply_no_match() throws JsonProcessingException {
    JsonMapper jsonMapper = JsonMapper.builder().build();
    JsonNode jsonNode = jsonMapper.readTree(EXAMPLE_JSON);
    Scrubber<JsonNode> itemScrubber = jsonPointer("/unknown");
    JsonNode scrubbedJsonNode = itemScrubber.apply(jsonNode);

    assertThat(scrubbedJsonNode).isEqualTo(jsonNode);
  }

  @Test
  void apply_custom_static_replacement() throws JsonProcessingException {
    JsonNode jsonNode = JSON_MAPPER.readTree(EXAMPLE_JSON);
    Scrubber<JsonNode> enrollmentDateScrubber =
        jsonPointer("/enrollmentDate").replacement("[scrubbed enrollment date]");

    JsonNode scrubbedJsonNode = enrollmentDateScrubber.apply(jsonNode);

    System.out.println(scrubbedJsonNode);

    assertThat(scrubbedJsonNode.at("/enrollmentDate").textValue())
        .isEqualTo("[scrubbed enrollment date]");
  }

  @Test
  void apply_custom_replacement() throws JsonProcessingException {
    JsonNode jsonNode = JSON_MAPPER.readTree(EXAMPLE_JSON);
    Scrubber<JsonNode> enrollmentDateScrubber =
        jsonPointer("/enrollmentDate").replacement("<enrollmentDate>"::formatted);

    JsonNode scrubbedJsonNode = enrollmentDateScrubber.apply(jsonNode);

    assertThat(scrubbedJsonNode.at("/enrollmentDate").textValue()).isEqualTo("<enrollmentDate>");
  }
}
