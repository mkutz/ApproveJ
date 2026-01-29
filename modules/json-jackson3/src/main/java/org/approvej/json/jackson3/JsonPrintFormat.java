package org.approvej.json.jackson3;

import org.approvej.configuration.Configuration;
import org.approvej.print.PrintFormat;
import org.approvej.print.PrintFormatProvider;
import org.approvej.print.Printer;
import org.jspecify.annotations.NullMarked;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;

/**
 * A {@link PrintFormat} that uses {@link ObjectWriter#writeValueAsString(Object)} to print a value
 * as JSON.
 *
 * @param <T> the type of the object to print
 */
@NullMarked
public final class JsonPrintFormat<T> implements PrintFormat<T>, PrintFormatProvider<T> {

  static {
    try {
      Class.forName("tools.jackson.databind.ObjectMapper");
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(
          "Jackson 3 is required but not found on classpath. "
              + "Add tools.jackson.core:jackson-databind to your dependencies.");
    }
  }

  private static final ObjectMapper DEFAULT_JSON_MAPPER = JsonMapper.builder().build();

  private final ObjectWriter objectWriter;
  private final ObjectReader objectReader;

  /**
   * Creates a {@link JsonPrintFormat} using the given {@link ObjectWriter} and {@link
   * ObjectReader}.
   *
   * @param objectWriter the {@link ObjectWriter} that will be used for printing
   * @param objectReader the {@link ObjectReader} that will be used to parse raw JSON {@link
   *     String}s for re-printing
   */
  JsonPrintFormat(ObjectWriter objectWriter, ObjectReader objectReader) {
    // Note: Jackson 3 defaults to WRITE_DATES_AS_TIMESTAMPS=false, so no need to disable it
    this.objectWriter = objectWriter;
    this.objectReader = objectReader;
  }

  /** Default constructor to be used in {@link Configuration}. */
  public JsonPrintFormat() {
    this(DEFAULT_JSON_MAPPER.writerWithDefaultPrettyPrinter(), DEFAULT_JSON_MAPPER.reader());
  }

  @Override
  public Printer<T> printer() {
    return (T value) -> {
      try {
        if (value instanceof String string) {
          return objectWriter.writeValueAsString(objectReader.readTree(string));
        }
        return objectWriter.writeValueAsString(value);
      } catch (JacksonException e) {
        throw new JsonPrinterException(value, e);
      }
    };
  }

  @Override
  public String filenameExtension() {
    return "json";
  }

  @Override
  public String alias() {
    return "json";
  }

  @Override
  public PrintFormat<T> create() {
    return json();
  }

  /**
   * Creates a {@link JsonPrintFormat} using the given {@link ObjectMapper}.
   *
   * @param objectMapper the {@link ObjectMapper} used to create the {@link ObjectWriter}
   * @param <T> the type of value to print
   * @return a new {@link JsonPrintFormat} instance
   * @see ObjectMapper#writerWithDefaultPrettyPrinter()
   */
  public static <T> JsonPrintFormat<T> json(ObjectMapper objectMapper) {
    return new JsonPrintFormat<>(
        objectMapper.writerWithDefaultPrettyPrinter(), objectMapper.reader());
  }

  /**
   * Creates a {@link JsonPrintFormat} using the default {@link JsonMapper}.
   *
   * @param <T> the type of value to print
   * @return a new {@link JsonPrintFormat} instance
   * @see JsonMapper.Builder#build()
   */
  public static <T> JsonPrintFormat<T> json() {
    return new JsonPrintFormat<>();
  }
}
