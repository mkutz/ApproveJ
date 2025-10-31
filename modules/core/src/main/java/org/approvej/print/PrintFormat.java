package org.approvej.print;

/**
 * A {@link Printer} that defines a format, including a fitting {@link #filenameExtension()}.
 *
 * @param <T> the type of the object to print
 */
public interface PrintFormat<T> extends Printer<T> {

  /** The default filename extension for files that the printed value is written to. */
  String DEFAULT_FILENAME_EXTENSION = "txt";

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
