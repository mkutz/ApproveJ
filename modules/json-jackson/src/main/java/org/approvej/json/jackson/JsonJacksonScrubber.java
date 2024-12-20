package org.approvej.json.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.approvej.Scrubber;

public class JsonJacksonScrubber implements Scrubber {

  private final JsonMapper jsonMapper;

  public JsonJacksonScrubber(JsonMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  public JsonJacksonScrubber() {
    this(JsonMapper.builder().build());
  }

  @Override
  public String apply(String unscrubbedValue) {
    try {
      var jsonNode = jsonMapper.readTree(unscrubbedValue);
      return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
