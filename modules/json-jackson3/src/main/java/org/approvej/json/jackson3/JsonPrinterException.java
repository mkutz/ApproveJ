package org.approvej.json.jackson3;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Exception thrown when pretty printing a value as JSON fails. */
@NullMarked
class JsonPrinterException extends RuntimeException {

  /**
   * Creates a new {@link JsonPrinterException} with the given message and cause.
   *
   * @param value the value that failed to be pretty printed
   * @param cause the cause of the failure
   */
  public JsonPrinterException(Object value, @Nullable Throwable cause) {
    super("Failed to pretty print %s".formatted(value), cause);
  }
}
