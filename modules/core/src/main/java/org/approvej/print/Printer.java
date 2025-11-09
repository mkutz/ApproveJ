package org.approvej.print;

import java.util.function.Function;

/**
 * A {@link Function} that converts an object to a {@link String}.
 *
 * @param <T> the type of the object to print
 */
public interface Printer<T> extends Function<T, String> {

  /** The default filename extension for files that the printed value is written to. */
  public String DEFAULT_FILENAME_EXTENSION = "txt";

  /**
   * Returns the suggested filename extension for the printed object. Defaults to {@value
   * DEFAULT_FILENAME_EXTENSION} if not overridden.
   *
   * @return the suggested filename extension.
   */
  default String filenameExtension() {
    return DEFAULT_FILENAME_EXTENSION;
  }
}
