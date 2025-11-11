package org.approvej.print;

import java.util.function.Function;

/**
 * A {@link Function} that converts an object to a {@link String}.
 *
 * @param <T> the type of the object to print
 */
public interface Printer<T> extends Function<T, String> {

  /**
   * The default filename extension for files that the printed value is written to.
   *
   * @deprecated use {@link PrintFormat#DEFAULT_FILENAME_EXTENSION}
   */
  @Deprecated String DEFAULT_FILENAME_EXTENSION = PrintFormat.DEFAULT_FILENAME_EXTENSION;

  /**
   * Returns the suggested filename extension for the printed object. Defaults to {@value
   * PrintFormat#DEFAULT_FILENAME_EXTENSION} if not overridden.
   *
   * @return the suggested filename extension
   * @deprecated use {@link PrintFormat}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  default String filenameExtension() {
    return PrintFormat.DEFAULT_FILENAME_EXTENSION;
  }
}
