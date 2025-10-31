package org.approvej.json.jackson;

/** Exception thrown when pretty printing a value as JSON fails. */
class JsonPrinterException extends RuntimeException {

  /**
   * Creates a new {@link JsonPrinterException} with the given message and cause.
   *
   * @param value the value that failed to be pretty printed
   * @param cause the cause of the failure
   */
  public JsonPrinterException(Object value, Throwable cause) {
    super("Failed to pretty print %s".formatted(value), cause);
  }
}
