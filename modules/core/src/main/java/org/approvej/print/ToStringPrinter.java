package org.approvej.print;

import org.jspecify.annotations.NullMarked;

/** A simple {@link Printer} implementation that uses the {@link Object#toString()} method. */
@NullMarked
public class ToStringPrinter implements Printer<Object> {

  @Override
  public String apply(Object value) {
    return value.toString();
  }
}
