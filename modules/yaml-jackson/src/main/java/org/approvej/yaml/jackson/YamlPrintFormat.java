package org.approvej.yaml.jackson;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.approvej.configuration.Configuration;
import org.approvej.print.PrintFormat;
import org.approvej.print.Printer;
import org.jspecify.annotations.NullMarked;

/**
 * A {@link PrintFormat} that uses {@link ObjectWriter#writeValueAsString(Object)} to print a value
 * as YAML.
 *
 * @param <T> the type of the object to print
 */
@NullMarked
public final class YamlPrintFormat<T> implements PrintFormat<T> {

  private static final YAMLMapper DEFAULT_YAML_MAPPER =
      YAMLMapper.builder().addModule(new JavaTimeModule()).build();

  private final ObjectWriter objectWriter;

  /**
   * Creates a {@link YamlPrintFormat} using the given {@link ObjectWriter}.
   *
   * @param objectWriter the {@link ObjectWriter} that will be used for printing
   */
  YamlPrintFormat(ObjectWriter objectWriter) {
    this.objectWriter = objectWriter.without(WRITE_DATES_AS_TIMESTAMPS);
  }

  /** Default constructor to be used in {@link Configuration}. */
  public YamlPrintFormat() {
    this(DEFAULT_YAML_MAPPER.writer());
  }

  @Override
  public Printer<T> printer() {
    return (T value) -> {
      try {
        return objectWriter.writeValueAsString(value);
      } catch (JsonProcessingException e) {
        throw new YamlPrinterException(value, e);
      }
    };
  }

  @Override
  public String filenameExtension() {
    return "yaml";
  }

  /**
   * Creates a {@link YamlPrintFormat} using the given {@link ObjectWriter}.
   *
   * @param objectWriter the {@link ObjectWriter} that will be used for printing
   * @param <T> the type of value to print
   * @return a new {@link YamlPrintFormat} instance
   */
  public static <T> YamlPrintFormat<T> yaml(ObjectWriter objectWriter) {
    return new YamlPrintFormat<>(objectWriter);
  }

  /**
   * Creates a {@link YamlPrintFormat} using the given {@link ObjectMapper}.
   *
   * @param objectMapper the {@link ObjectMapper} used to create the {@link ObjectWriter}
   * @param <T> the type of value to print
   * @return a new {@link YamlPrintFormat} instance
   * @see ObjectMapper#writerWithDefaultPrettyPrinter()
   */
  public static <T> YamlPrintFormat<T> yaml(ObjectMapper objectMapper) {
    return yaml(objectMapper.writer());
  }

  /**
   * Creates a {@link YamlPrintFormat} using the default {@link YAMLMapper}.
   *
   * @param <T> the type of value to print
   * @return a new {@link YamlPrintFormat} instance
   * @see YAMLMapper.Builder#build()
   */
  public static <T> YamlPrintFormat<T> yaml() {
    return yaml(DEFAULT_YAML_MAPPER);
  }
}
