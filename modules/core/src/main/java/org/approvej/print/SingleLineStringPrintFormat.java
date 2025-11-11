package org.approvej.print;

import org.approvej.Configuration;
import org.jspecify.annotations.NonNull;

/**
 * A generic print format for Java {@link Object}s that prints that uses the {@link
 * Object#toString()} method.
 */
public final class SingleLineStringPrintFormat implements PrintFormat<Object> {

  /** Default constructor to be used in {@link Configuration}. */
  public SingleLineStringPrintFormat() {}

  @Override
  @NonNull
  public Printer<Object> printer() {
    return String::valueOf;
  }

  /**
   * Creates a new {@link SingleLineStringPrintFormat}.
   *
   * @return a new {@link SingleLineStringPrintFormat}
   */
  @NonNull
  public static SingleLineStringPrintFormat singleLineString() {
    return new SingleLineStringPrintFormat();
  }
}
