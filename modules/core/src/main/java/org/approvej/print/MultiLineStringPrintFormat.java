package org.approvej.print;

import org.approvej.Configuration;
import org.jspecify.annotations.NullMarked;

/**
 * A generic printer for Java {@link Object}s that prints their properties and values one per line.
 */
@NullMarked
public final class MultiLineStringPrintFormat implements PrintFormat<Object> {

  private final MultiLineStringPrinter<Object> printer;

  /**
   * @param printer the printer implementing this format
   */
  MultiLineStringPrintFormat(MultiLineStringPrinter<Object> printer) {
    this.printer = printer;
  }

  /** Default constructor to be used in {@link Configuration}. */
  public MultiLineStringPrintFormat() {
    this(new MultiLineStringPrinter<>());
  }

  @Override
  public Printer<Object> printer() {
    return printer;
  }

  /**
   * Sort the printed object's fields alphabetically by their name. By default, the fields will be
   * printed in the order of their declaration.
   *
   * @return a copy of this sorting fields by name
   */
  public MultiLineStringPrintFormat sorted() {
    return new MultiLineStringPrintFormat(printer.sorted());
  }

  /**
   * Creates a new {@link MultiLineStringPrintFormat}.
   *
   * @return a new {@link MultiLineStringPrintFormat}
   */
  public static MultiLineStringPrintFormat multiLineString() {
    return new MultiLineStringPrintFormat();
  }
}
