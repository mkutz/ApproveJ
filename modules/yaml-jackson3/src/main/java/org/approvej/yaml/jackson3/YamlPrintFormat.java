package org.approvej.yaml.jackson3;

import org.approvej.configuration.Configuration;
import org.approvej.print.PrintFormat;
import org.approvej.print.PrintFormatProvider;
import org.approvej.print.Printer;
import org.jspecify.annotations.NullMarked;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.dataformat.yaml.YAMLMapper;

/**
 * A {@link PrintFormat} that uses {@link ObjectWriter#writeValueAsString(Object)} to print a value
 * as YAML.
 *
 * @param objectWriter the {@link ObjectWriter} that will be used for printing
 * @param <T> the type of the object to print
 */
@NullMarked
public record YamlPrintFormat<T>(ObjectWriter objectWriter)
    implements PrintFormat<T>, PrintFormatProvider<T> {

  static {
    try {
      Class.forName("tools.jackson.dataformat.yaml.YAMLMapper");
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(
          "Jackson 3 YAML is required but not found on classpath. "
              + "Add tools.jackson.dataformat:jackson-dataformat-yaml to your dependencies.");
    }
  }

  // Note: Jackson 3 has Java date/time support built-in, no need for JavaTimeModule
  private static final YAMLMapper DEFAULT_YAML_MAPPER = YAMLMapper.builder().build();

  /**
   * Creates a {@link YamlPrintFormat} using the given {@link ObjectWriter}.
   *
   * @param objectWriter the {@link ObjectWriter} that will be used for printing
   */
  public YamlPrintFormat {
    // Note: Jackson 3 defaults to WRITE_DATES_AS_TIMESTAMPS=false, so no need to disable it
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
      } catch (JacksonException e) {
        throw new YamlPrinterException(value, e);
      }
    };
  }

  @Override
  public String filenameExtension() {
    return "yaml";
  }

  @Override
  public String alias() {
    return "yaml";
  }

  @Override
  public PrintFormat<T> create() {
    return yaml();
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
