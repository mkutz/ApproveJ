package org.approvej.json.jackson;

import static org.approvej.json.jackson.JsonPrinter.DEFAULT_JSON_MAPPER;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.approvej.Configuration;
import org.approvej.print.PrintFormat;
import org.approvej.print.Printer;
import org.jspecify.annotations.NullMarked;

/**
 * A generic printer for Java {@link Object}s that uses an {@link ObjectMapper}.
 *
 * @param <T> the type of the object to print
 */
@NullMarked
public final class JsonPrintFormat<T> implements PrintFormat<T> {
  private final Printer<T> printer;

  JsonPrintFormat(Printer<T> printer) {
    this.printer = printer;
  }

  /** Default constructor to be used in {@link Configuration}. */
  public JsonPrintFormat() {
    this(
        new JsonPrinter<>(
            DEFAULT_JSON_MAPPER.writerWithDefaultPrettyPrinter(), DEFAULT_JSON_MAPPER.reader()));
  }

  @Override
  public Printer<T> printer() {
    return printer;
  }

  @Override
  public String filenameExtension() {
    return "json";
  }

  /**
   * Creates a {@link JsonPrinter} using the given {@link ObjectMapper}.
   *
   * @param objectMapper the {@link ObjectMapper} used to create the {@link ObjectWriter}
   * @param <T> the type of value to print
   * @return a new {@link JsonPrinter} instance
   * @see ObjectMapper#writerWithDefaultPrettyPrinter()
   */
  public static <T> JsonPrintFormat<T> json(ObjectMapper objectMapper) {
    return new JsonPrintFormat<>(
        new JsonPrinter<>(objectMapper.writerWithDefaultPrettyPrinter(), objectMapper.reader()));
  }

  /**
   * Creates a {@link JsonPrinter} using the default {@link JsonMapper}.
   *
   * @param <T> the type of value to print
   * @return a new {@link JsonPrinter} instance
   * @see JsonMapper.Builder#build()
   */
  public static <T> JsonPrintFormat<T> json() {
    return new JsonPrintFormat<>();
  }
}
