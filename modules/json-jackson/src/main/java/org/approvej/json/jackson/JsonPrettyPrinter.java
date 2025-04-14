package org.approvej.json.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.approvej.print.Printer;
import org.jspecify.annotations.NullMarked;

/**
 * A {@link Printer} that uses {@link ObjectWriter#writeValueAsString(Object)} to print a value as
 * JSON.
 *
 * @param <T> the type of value to print
 */
@NullMarked
public class JsonPrettyPrinter<T> implements Printer<T> {

  private final ObjectWriter objectWriter;

  /**
   * Creates a {@link JsonPrettyPrinter} using the given {@link ObjectWriter}.
   *
   * @param objectWriter the {@link ObjectWriter} that will be used for printing
   * @param <T> the type of value to print
   * @return a new {@link JsonPrettyPrinter} instance
   */
  public static <T> JsonPrettyPrinter<T> jsonPrettyPrinter(ObjectWriter objectWriter) {
    return new JsonPrettyPrinter<>(objectWriter);
  }

  /**
   * Creates a {@link JsonPrettyPrinter} using the given {@link ObjectMapper}.
   *
   * @param objectMapper the {@link ObjectMapper} used to create the {@link ObjectWriter}
   * @param <T> the type of value to print
   * @return a new {@link JsonPrettyPrinter} instance
   * @see ObjectMapper#writerWithDefaultPrettyPrinter()
   */
  public static <T> JsonPrettyPrinter<T> jsonPrettyPrinter(ObjectMapper objectMapper) {
    return new JsonPrettyPrinter<>(objectMapper.writerWithDefaultPrettyPrinter());
  }

  /**
   * Creates a {@link JsonPrettyPrinter} using the default {@link JsonMapper}.
   *
   * @return a new {@link JsonPrettyPrinter} instance
   * @param <T> the type of value to print
   * @see JsonMapper.Builder#build()
   */
  public static <T> JsonPrettyPrinter<T> jsonPrettyPrinter() {
    return new JsonPrettyPrinter<>(JsonMapper.builder().build().writerWithDefaultPrettyPrinter());
  }

  private JsonPrettyPrinter(ObjectWriter objectWriter) {
    this.objectWriter = objectWriter;
  }

  @Override
  public String apply(T value) {
    try {
      return objectWriter.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new JsonPrettyPrinterException(value, e);
    }
  }
}
