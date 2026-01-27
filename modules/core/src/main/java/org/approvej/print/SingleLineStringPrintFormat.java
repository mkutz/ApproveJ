package org.approvej.print;

import org.jspecify.annotations.NullMarked;

/**
 * A generic print format for Java {@link Object}s that prints that uses the {@link
 * Object#toString()} method.
 */
@NullMarked
public record SingleLineStringPrintFormat()
    implements PrintFormat<Object>, PrintFormatProvider<Object> {

  /** The alias for this print format used in configuration. */
  public static final String ALIAS = "singleLineString";

  @Override
  public Printer<Object> printer() {
    return "%s"::formatted;
  }

  @Override
  public String alias() {
    return ALIAS;
  }

  @Override
  public PrintFormat<Object> create() {
    return new SingleLineStringPrintFormat();
  }

  /**
   * Creates a new {@link SingleLineStringPrintFormat}.
   *
   * @return a new {@link SingleLineStringPrintFormat}
   */
  public static SingleLineStringPrintFormat singleLineString() {
    return new SingleLineStringPrintFormat();
  }
}
