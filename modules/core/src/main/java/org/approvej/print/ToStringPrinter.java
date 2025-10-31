package org.approvej.print;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** A simple {@link PrintFormat} implementation that uses the {@link Object#toString()} method. */
@NullMarked
public class ToStringPrinter implements PrintFormat<Object> {

  /**
   * Creates a new {@link ToStringPrinter}.
   *
   * <p>This constructor is public to allow instantiation via reflection, e.g. in the {@link
   * org.approvej.Configuration} class.
   */
  public ToStringPrinter() {
    // No initialization needed
  }

  @Override
  public String apply(@Nullable Object value) {
    return "%s".formatted(value);
  }
}
