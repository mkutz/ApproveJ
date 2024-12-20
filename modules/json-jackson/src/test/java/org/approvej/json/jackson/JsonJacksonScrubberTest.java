package org.approvej.json.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JsonJacksonScrubberTest {

  @Test
  void apply() {
    var unscrubbedValue = "{\"name\":\"John\"}";
    var jsonJacksonScrubber = new JsonJacksonScrubber();
    assertThat(jsonJacksonScrubber.apply(unscrubbedValue)).isEqualTo("{\n  \"name\" : \"John\"\n}");
  }
}
