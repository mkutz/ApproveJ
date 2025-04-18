package org.approvej.yaml.jackson;

import com.fasterxml.jackson.core.JacksonException;

public class YamlPrinterException extends RuntimeException {

  public YamlPrinterException(JacksonException cause) {
    super(cause);
  }
}
