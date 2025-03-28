package org.approvej.print;

/**
 * A {@link Printer} that simply calls {@link Object#toString()} on the value.
 *
 * @param <T> the type of value to print
 */
public class ToStringPrinter<T> implements Printer<T> {

  @Override
  public String apply(T value) {
    return value.toString();
  }
}
