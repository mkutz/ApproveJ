package org.approvej.json.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.approvej.Printer;

public class JsonPrettyPrinter<T> implements Printer<T> {

  private final ObjectWriter objectWriter;

  public JsonPrettyPrinter(JsonMapper jsonMapper) {
    this.objectWriter = jsonMapper.writerWithDefaultPrettyPrinter();
  }

  @Override
  public String apply(T value) {
    try {
      return objectWriter.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
