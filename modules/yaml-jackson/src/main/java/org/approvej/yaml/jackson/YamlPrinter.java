package org.approvej.yaml.jackson;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.approvej.print.Printer;
import org.jspecify.annotations.NullMarked;

/**
 * A {@link Printer} that uses {@link ObjectWriter#writeValueAsString(Object)} to print a value as
 * YAML.
 *
 * @param <T> the type of value to print
 */
@NullMarked
public class YamlPrinter<T> implements Printer<T> {

  static final YAMLMapper DEFAULT_YAML_MAPPER =
      YAMLMapper.builder().addModule(new JavaTimeModule()).build();

  private final ObjectWriter objectWriter;

  /**
   * Creates a {@link YamlPrinter} using the given {@link ObjectWriter}.
   *
   * @param objectWriter the {@link ObjectWriter} that will be used for printing
   */
  YamlPrinter(ObjectWriter objectWriter) {
    this.objectWriter = objectWriter.without(WRITE_DATES_AS_TIMESTAMPS);
  }

  @Override
  public String apply(T value) {
    try {
      return objectWriter.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new YamlPrinterException(value, e);
    }
  }

  @SuppressWarnings("removal")
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
   * @deprecated use {@link YamlPrintFormat#yaml(ObjectWriter)}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public static <T> YamlPrinter<T> yamlPrinter(ObjectWriter objectWriter) {
    return new YamlPrinter<>(objectWriter);
  }

  /**
   * Creates a {@link YamlPrinter} using the given {@link ObjectMapper}.
   *
   * @param objectMapper the {@link ObjectMapper} used to create the {@link ObjectWriter}
   * @param <T> the type of value to print
   * @return a new {@link YamlPrinter} instance
   * @see ObjectMapper#writerWithDefaultPrettyPrinter()
   * @deprecated use {@link YamlPrintFormat#yaml(ObjectMapper)}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public static <T> YamlPrinter<T> yamlPrinter(ObjectMapper objectMapper) {
    return yamlPrinter(objectMapper.writer());
  }

  /**
   * Creates a {@link YamlPrinter} using the default {@link YAMLMapper}.
   *
   * @return a new {@link YamlPrinter} instance
   * @param <T> the type of value to print
   * @see YAMLMapper.Builder#build()
   * @deprecated use {@link YamlPrintFormat#yaml()}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public static <T> YamlPrinter<T> yamlPrinter() {
    return new YamlPrinter<>(DEFAULT_YAML_MAPPER.writer());
  }
}
