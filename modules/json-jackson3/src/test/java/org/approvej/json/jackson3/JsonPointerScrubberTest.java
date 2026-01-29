package org.approvej.json.jackson3;

import static java.util.UUID.randomUUID;
import static org.approvej.json.jackson3.JsonPointerScrubber.jsonPointer;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

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
  void apply() throws JacksonException {
    JsonMapper jsonMapper = JsonMapper.builder().build();
    JsonNode jsonNode = jsonMapper.readTree(EXAMPLE_JSON);
    JsonPointerScrubber idScrubber = jsonPointer("/id");
    JsonNode scrubbedJsonNode = idScrubber.apply(jsonNode);

    assertThat(scrubbedJsonNode.at("/id").stringValue()).isEqualTo("[scrubbed]");
  }

  @Test
  void apply_deeper_value() throws JacksonException {
    JsonMapper jsonMapper = JsonMapper.builder().build();
    JsonNode jsonNode = jsonMapper.readTree(EXAMPLE_JSON);
    JsonPointerScrubber itemScrubber = jsonPointer("/items/0/itemId");
    JsonNode scrubbedJsonNode = itemScrubber.apply(jsonNode);

    assertThat(scrubbedJsonNode.at("/items/0/itemId").stringValue()).isEqualTo("[scrubbed]");
  }

  @Test
  void apply_subtree() throws JacksonException {
    JsonMapper jsonMapper = JsonMapper.builder().build();
    JsonNode jsonNode = jsonMapper.readTree(EXAMPLE_JSON);
    JsonPointerScrubber itemScrubber = jsonPointer("/items/0");
    JsonNode scrubbedJsonNode = itemScrubber.apply(jsonNode);

    assertThat(scrubbedJsonNode.at("/items/0").stringValue()).isEqualTo("[scrubbed]");
  }

  @Test
  void apply_no_match() throws JacksonException {
    JsonMapper jsonMapper = JsonMapper.builder().build();
    JsonNode jsonNode = jsonMapper.readTree(EXAMPLE_JSON);
    JsonPointerScrubber itemScrubber = jsonPointer("/unknown");
    JsonNode scrubbedJsonNode = itemScrubber.apply(jsonNode);

    assertThat(scrubbedJsonNode).isEqualTo(jsonNode);
  }

  @Test
  void apply_custom_static_replacement() throws JacksonException {
    JsonNode jsonNode = JSON_MAPPER.readTree(EXAMPLE_JSON);
    JsonPointerScrubber enrollmentDateScrubber =
        jsonPointer("/enrollmentDate").replacement("[scrubbed enrollment date]");

    JsonNode scrubbedJsonNode = enrollmentDateScrubber.apply(jsonNode);

    assertThat(scrubbedJsonNode.at("/enrollmentDate").stringValue())
        .isEqualTo("[scrubbed enrollment date]");
  }

  @Test
  void apply_custom_replacement() throws JacksonException {
    JsonNode jsonNode = JSON_MAPPER.readTree(EXAMPLE_JSON);
    JsonPointerScrubber enrollmentDateScrubber =
        jsonPointer("/enrollmentDate").replacement("<enrollmentDate>"::formatted);

    JsonNode scrubbedJsonNode = enrollmentDateScrubber.apply(jsonNode);

    assertThat(scrubbedJsonNode.at("/enrollmentDate").stringValue()).isEqualTo("<enrollmentDate>");
  }
}
