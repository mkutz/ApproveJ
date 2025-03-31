package org.approvej.json.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.approvej.print.Printer;

/**
 * A {@link Printer} that uses {@link ObjectWriter#writeValueAsString(Object)} to print a value as
 * JSON.
 *
 * @param <T> the type of value to print
 */
public class JsonPrettyPrinter<T> implements Printer<T> {

  private final ObjectWriter objectWriter;

  /**
   * Creates a {@link JsonPrettyPrinter} using the given {@link JsonMapper}.
   *
   * @param jsonMapper the {@link JsonMapper} used to create the {@link ObjectWriter}
   */
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
