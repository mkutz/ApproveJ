package org.approvej.json.jackson;

import java.util.function.Function;
import org.approvej.print.Printer;

/**
 * A {@link Function} that converts an object to a {@link String} in JSON format.
 *
 * @param <T> the type of the object to print
 */
public interface JsonPrinter<T> extends Printer<T> {

  @Override
  default String filenameExtension() {
    return "json";
  }
}
