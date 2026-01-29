package org.approvej.yaml.jackson3;

import tools.jackson.core.JacksonException;

/** Exception thrown when pretty printing a value as YAML fails. */
public class YamlPrinterException extends RuntimeException {

  /**
   * Creates a new {@link YamlPrinterException} with the given cause.
   *
   * @param value the value that failed to be pretty printed
   * @param cause the cause of the failure
   */
  public YamlPrinterException(Object value, JacksonException cause) {
    super("Failed to print %s".formatted(value), cause);
  }
}
