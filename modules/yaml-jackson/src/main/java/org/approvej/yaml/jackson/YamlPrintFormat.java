package org.approvej.yaml.jackson;

import static org.approvej.yaml.jackson.YamlPrinter.DEFAULT_YAML_MAPPER;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.approvej.Configuration;
import org.approvej.print.PrintFormat;
import org.approvej.print.Printer;
import org.jspecify.annotations.NullMarked;

/**
 * A generic printer for Java {@link Object}s that uses an {@link YamlPrinter}.
 *
 * @param <T> the type of the object to print
 */
@NullMarked
public final class YamlPrintFormat<T> implements PrintFormat<T> {

  private final Printer<T> printer;

  YamlPrintFormat(Printer<T> printer) {
    this.printer = printer;
  }

  /** Default constructor to be used in {@link Configuration}. */
  public YamlPrintFormat() {
    this(new YamlPrinter<>(DEFAULT_YAML_MAPPER.writer()));
  }

  @Override
  public Printer<T> printer() {
    return printer;
  }

  @Override
  public String filenameExtension() {
    return "yaml";
  }

  /**
   * Creates a {@link YamlPrinter} using the given {@link ObjectWriter}.
   *
   * @param objectWriter the {@link ObjectWriter} that will be used for printing
   * @param <T> the type of value to print
   * @return a new {@link YamlPrinter} instance
   */
  public static <T> YamlPrintFormat<T> yaml(ObjectWriter objectWriter) {
    return new YamlPrintFormat<>(new YamlPrinter<>(objectWriter));
  }

  /**
   * Creates a {@link YamlPrinter} using the given {@link ObjectMapper}.
   *
   * @param objectMapper the {@link ObjectMapper} used to create the {@link ObjectWriter}
   * @param <T> the type of value to print
   * @return a new {@link YamlPrinter} instance
   * @see ObjectMapper#writerWithDefaultPrettyPrinter()
   */
  public static <T> YamlPrintFormat<T> yaml(ObjectMapper objectMapper) {
    return yaml(objectMapper.writer());
  }

  /**
   * Creates a {@link YamlPrinter} using the default {@link YAMLMapper}.
   *
   * @param <T> the type of value to print
   * @return a new {@link YamlPrinter} instance
   * @see YAMLMapper.Builder#build()
   */
  public static <T> YamlPrintFormat<T> yaml() {
    return yaml(DEFAULT_YAML_MAPPER);
  }
}
