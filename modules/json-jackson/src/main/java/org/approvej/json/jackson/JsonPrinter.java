package org.approvej.json.jackson;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.approvej.print.Printer;
import org.jspecify.annotations.NullMarked;

/**
 * A {@link Printer} that uses {@link ObjectWriter#writeValueAsString(Object)} to print a value as
 * JSON.
 *
 * @param <T> the type of value to print
 */
@NullMarked
public class JsonPrinter<T> implements Printer<T> {

  static final ObjectMapper DEFAULT_JSON_MAPPER =
      JsonMapper.builder().addModule(new JavaTimeModule()).build();

  private final ObjectWriter objectWriter;
  private final ObjectReader objectReader;

  /**
   * Creates a {@link JsonPrinter} using the given {@link ObjectWriter} and {@link ObjectReader}.
   *
   * @param objectWriter the {@link ObjectWriter} that will be used for printing
   * @param objectReader the {@link ObjectReader} that will be used to parse raw JSON {@link
   *     String}s for re-printing
   */
  JsonPrinter(ObjectWriter objectWriter, ObjectReader objectReader) {
    this.objectWriter = objectWriter.without(WRITE_DATES_AS_TIMESTAMPS);
    this.objectReader = objectReader;
  }

  @Override
  public String apply(Object value) {
    try {
      if (value instanceof String string) {
        return apply(objectReader.readTree(string));
      }
      return objectWriter.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new JsonPrinterException(value, e);
    }
  }

  /**
   * Creates a {@link JsonPrinter} using the given {@link ObjectWriter}.
   *
   * @param objectWriter the {@link ObjectWriter} that will be used for printing
   * @param <T> the type of value to print
   * @return a new {@link JsonPrinter} instance
   * @deprecated use {@link JsonPrintFormat#json(ObjectMapper)}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public static <T> JsonPrinter<T> jsonPrettyPrinter(ObjectWriter objectWriter) {
    return new JsonPrinter<>(objectWriter, DEFAULT_JSON_MAPPER.reader());
  }

  /**
   * Creates a {@link JsonPrinter} using the given {@link ObjectMapper}.
   *
   * @param objectMapper the {@link ObjectMapper} used to create the {@link ObjectWriter}
   * @param <T> the type of value to print
   * @return a new {@link JsonPrinter} instance
   * @see ObjectMapper#writerWithDefaultPrettyPrinter()
   * @deprecated use {@link JsonPrintFormat#json(ObjectMapper)}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public static <T> JsonPrinter<T> jsonPrettyPrinter(ObjectMapper objectMapper) {
    return new JsonPrinter<>(objectMapper.writerWithDefaultPrettyPrinter(), objectMapper.reader());
  }

  /**
   * Creates a {@link JsonPrinter} using the default {@link JsonMapper}.
   *
   * @return a new {@link JsonPrinter} instance
   * @param <T> the type of value to print
   * @see JsonMapper.Builder#build()
   * @deprecated use {@link JsonPrintFormat#json()}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public static <T> JsonPrinter<T> jsonPrettyPrinter() {
    return jsonPrettyPrinter(DEFAULT_JSON_MAPPER);
  }
}
