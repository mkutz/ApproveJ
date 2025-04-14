package org.approvej.json.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.approvej.print.Printer;
import org.jspecify.annotations.NullMarked;

/** A {@link Printer} for JSON strings that should be pretty-printed. */
@NullMarked
public class JsonStringPrettyPrinter implements Printer<String> {

  private final ObjectReader objectReader;
  private final ObjectWriter objectWriter;

  /**
   * Creates a {@link JsonStringPrettyPrinter} using the given {@link ObjectMapper}.
   *
   * @param objectMapper the {@link ObjectMapper} used to create the {@link ObjectReader} and {@link
   *     ObjectWriter}.
   * @see ObjectMapper#reader()
   * @see ObjectMapper#writerWithDefaultPrettyPrinter()
   */
  public JsonStringPrettyPrinter(ObjectMapper objectMapper) {
    this.objectReader = objectMapper.reader();
    this.objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
  }

  /** Creates a {@link JsonStringPrettyPrinter} using the default {@link JsonMapper}. */
  public JsonStringPrettyPrinter() {
    this(JsonMapper.builder().build());
  }

  @Override
  public String apply(String value) {
    try {
      return objectWriter.writeValueAsString(objectReader.readTree(value));
    } catch (JsonProcessingException e) {
      throw new JsonPrettyPrinterException(value, e);
    }
  }
}
