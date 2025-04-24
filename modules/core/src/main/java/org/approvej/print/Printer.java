package org.approvej.print;

import static org.approvej.approve.PathProvider.DEFAULT_FILENAME_EXTENSION;

import java.util.function.Function;

/**
 * A {@link Function} that converts an object to a {@link String}.
 *
 * @param <T> the type of the object to print
 */
public interface Printer<T> extends Function<T, String> {

  /**
   * Returns the suggested filename extension for the printed object. Defaults to "txt" if not
   * overridden.
   *
   * @return the suggested filename extension.
   */
  default String filenameExtension() {
    return DEFAULT_FILENAME_EXTENSION;
  }
}
