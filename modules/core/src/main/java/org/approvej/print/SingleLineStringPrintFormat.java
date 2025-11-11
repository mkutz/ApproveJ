package org.approvej.print;

import org.jspecify.annotations.NonNull;

/*
 * A generic printer for Java {@link Object}s that prints that uses the {@link Object#toString()} method.
 */
public final class SingleLineStringPrintFormat implements PrintFormat<Object> {

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
