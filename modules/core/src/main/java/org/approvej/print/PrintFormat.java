package org.approvej.print;

/**
 * A format to print values of type T, as defined by a {@link Printer} and a suggested {@link
 * #filenameExtension()}.
 *
 * @param <T> the type of the object to print
 */
public interface PrintFormat<T> {

  /** The default filename extension for files that the printed value is written to. */
  String DEFAULT_FILENAME_EXTENSION = "txt";

  /**
   * Returns the printer implementing this format.
   *
   * @return the printer implementing this format
   */
  Printer<T> printer();

  /**
   * Returns the suggested filename extension for the printed object. Defaults to {@value
   * DEFAULT_FILENAME_EXTENSION} if not overridden.
   *
   * @return the suggested filename extension
   */
  default String filenameExtension() {
    return DEFAULT_FILENAME_EXTENSION;
  }
}
