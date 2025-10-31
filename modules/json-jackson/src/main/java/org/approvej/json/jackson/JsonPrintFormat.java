package org.approvej.json.jackson;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.approvej.print.PrintFormat;
import org.approvej.print.Printer;

/**
 * A {@link Printer} that prints values as pretty-printed JSON using {@link
 * ObjectWriter#writeValueAsString(Object)}.
 *
 * <p>This printer handles both arbitrary Java objects and JSON strings:
 *
 * <ul>
 *   <li>If the input is a Java object (other than {@link String}), it is serialized to
 *       pretty-printed JSON.
 *   <li>If the input is a {@link String}, it is assumed to be a JSON string, which is parsed and
 *       then pretty-printed. If the string is not valid JSON, a {@link JsonPrinterException} is
 *       thrown.
 * </ul>
 *
 * This special handling allows pretty-printing of raw JSON strings as well as Java objects.
 */
public class JsonPrintFormat implements PrintFormat<Object> {

  private ObjectMapper objectMapper;

  /**
   * Creates a {@link JsonPrintFormat} using the default {@link JsonMapper}.
   *
   * @return the new {@link JsonPrintFormat}
   */
  public static JsonPrintFormat json() {
    return new JsonPrintFormat();
  }

  /** Creates a {@link JsonPrintFormat} using the default {@link JsonMapper}. */
  public JsonPrintFormat() {
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
  public JsonPrintFormat using(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    return this;
  }

  @Override
  public String filenameExtension() {
    return "json";
  }
}
