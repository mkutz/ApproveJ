package org.approvej.json.jackson;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.approvej.print.Printer;

/**
 * A {@link Printer} that uses {@link ObjectWriter#writeValueAsString(Object)} to print a value as
 * JSON.
 */
public class JsonPrinter implements Printer<Object> {

  private ObjectMapper objectMapper;

  /**
   * Creates a {@link JsonPrinter} using the default {@link JsonMapper}.
   *
   * @return the new {@link JsonPrinter}
   */
  public static JsonPrinter json() {
    return new JsonPrinter();
  }

  /** Creates a {@link JsonPrinter} using the default {@link JsonMapper}. */
  public JsonPrinter() {
    this.objectMapper =
        JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(WRITE_DATES_AS_TIMESTAMPS)
            .build();
  }

  @Override
  public String apply(Object value) {
    try {
      if (value instanceof String string) {
        return apply(objectMapper.readTree(string));
      }
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new JsonPrinterException(value, e);
    }
  }

  /**
   * Sets the used {@link ObjectMapper} to the given instance.
   *
   * @param objectMapper the {@link ObjectMapper} used to create the {@link ObjectWriter}
   * @return this
   */
  public JsonPrinter using(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    return this;
  }

  @Override
  public String filenameExtension() {
    return "json";
  }
}
