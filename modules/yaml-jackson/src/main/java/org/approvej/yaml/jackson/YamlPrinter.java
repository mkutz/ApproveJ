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
 */
@NullMarked
public class YamlPrinter implements Printer<Object> {

  private ObjectWriter objectWriter;

  /**
   * Creates a {@link YamlPrinter} using the given {@link ObjectWriter}.
   *
   * @param objectWriter the {@link ObjectWriter} that will be used for printing
   * @return a new {@link YamlPrinter} instance
   * @deprecated use {@link #yaml()} + {@link #using(ObjectWriter)}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public static YamlPrinter yamlPrinter(ObjectWriter objectWriter) {
    return yaml().using(objectWriter);
  }

  /**
   * Creates a {@link YamlPrinter} using the given {@link ObjectMapper}.
   *
   * @param objectMapper the {@link ObjectMapper} used to create the {@link ObjectWriter}
   * @return a new {@link YamlPrinter} instance
   * @see ObjectMapper#writer()
   * @deprecated use {@link #yaml()} + {@link #using(ObjectMapper)}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public static YamlPrinter yamlPrinter(ObjectMapper objectMapper) {
    return yaml().using(objectMapper);
  }

  /**
   * Creates a {@link YamlPrinter} using the default {@link YAMLMapper}.
   *
   * @return a new {@link YamlPrinter} instance
   * @see YAMLMapper.Builder#build()
   */
  public static YamlPrinter yaml() {
    return new YamlPrinter();
  }

  /**
   * Creates a {@link YamlPrinter} using the default {@link YAMLMapper}.
   *
   * @return a new {@link YamlPrinter} instance
   * @see YAMLMapper.Builder#build()
   * @deprecated use {@link #yaml()}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public static YamlPrinter yamlPrinter() {
    return new YamlPrinter();
  }

  /** Creates a {@link YamlPrinter} using the default {@link YAMLMapper}. */
  public YamlPrinter() {
    this.objectWriter =
        YAMLMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(WRITE_DATES_AS_TIMESTAMPS)
            .build()
            .writer();
  }

  @Override
  public String apply(Object value) {
    try {
      return objectWriter.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new YamlPrinterException(value, e);
    }
  }

  /**
   * Sets the used {@link ObjectWriter} to the given instance.
   *
   * @param objectWriter the {@link ObjectWriter} to be used
   * @return this
   */
  public YamlPrinter using(ObjectWriter objectWriter) {
    this.objectWriter = objectWriter;
    return this;
  }

  /**
   * Sets the used {@link ObjectWriter} to an instance generated from the given {@link
   * ObjectMapper}.
   *
   * @param objectMapper the {@link ObjectMapper} used to create the {@link ObjectWriter}
   * @return this
   */
  public YamlPrinter using(ObjectMapper objectMapper) {
    return using(objectMapper.writer());
  }

  @Override
  public String filenameExtension() {
    return "yaml";
  }
}
