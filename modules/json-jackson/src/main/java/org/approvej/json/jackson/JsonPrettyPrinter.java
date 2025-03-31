package org.approvej.json.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
   * Creates a {@link JsonPrettyPrinter} using the given {@link ObjectWriter}.
   *
   * @param objectWriter the {@link ObjectWriter} that will be used for printing
   */
  public JsonPrettyPrinter(ObjectWriter objectWriter) {
    this.objectWriter = objectWriter;
  }

  /**
   * Creates a {@link JsonPrettyPrinter} using the given {@link ObjectMapper}.
   *
   * @param objectMapper the {@link ObjectMapper} used to create the {@link ObjectWriter}
   * @see ObjectMapper#writerWithDefaultPrettyPrinter()
   */
  public JsonPrettyPrinter(ObjectMapper objectMapper) {
    this(objectMapper.writerWithDefaultPrettyPrinter());
  }

  /**
   * Creates a {@link JsonPrettyPrinter} using the default {@link ObjectMapper}.
   *
   * @see JsonMapper.Builder#build()
   */
  public JsonPrettyPrinter() {
    this(JsonMapper.builder().build().writerWithDefaultPrettyPrinter());
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
