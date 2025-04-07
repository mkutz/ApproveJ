package org.approvej.json.jackson;

class JsonPrettyPrinterException extends RuntimeException {
  public JsonPrettyPrinterException(Object value, Throwable cause) {
    super("Failed to pretty print %s".formatted(value), cause);
  }
}
