package org.approvej.print;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** A simple {@link Printer} implementation that uses the {@link Object#toString()} method. */
@NullMarked
public class ToStringPrinter implements Printer<Object> {

  /** Creates a {@link ToStringPrinter}. */
  public ToStringPrinter() {}

  @Override
  public String apply(@Nullable Object value) {
    return "%s".formatted(value);
  }
}
