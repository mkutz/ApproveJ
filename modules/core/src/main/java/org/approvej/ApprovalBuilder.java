package org.approvej;

import static org.approvej.verify.FileVerifier.file;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.approvej.print.Printer;
import org.approvej.scrub.Scrubber;
import org.approvej.verify.Verifier;
import org.jspecify.annotations.NullMarked;

/**
 * A builder to configure an approval for a given value.
 *
 * <p>Optionally the value can be "scrubbed" of dynamic data (like timestamps or ID's).
 *
 * <p>The value will be printed (converted to {@link String}) using a {@link Printer}. By default,
 * the {@link org.approvej.print.ObjectPrinter} will be applied, which can be changed with the
 * {@link #printWith(Function)}.
 *
 * @param <T> the type of the value to approve
 */
@NullMarked
public class ApprovalBuilder<T> {

  /** The default {@link Printer} used to print the value. */
  public static final Printer<Object> DEFAULT_PRINTER = Object::toString;

  private T value;

  private ApprovalBuilder(T originalValue) {
    this.value = originalValue;
  }

  /**
   * Creates a new builder for the given value.
   *
   * @param originalValue the value to approve
   * @return a new {@link ApprovalBuilder} for the given value
   * @param <T> the type of the value to approve
   */
  public static <T> ApprovalBuilder<T> approve(T originalValue) {
    return new ApprovalBuilder<>(originalValue);
  }

  /**
   * Uses the given {@link Printer} to convert the value to a {@link String}.
   *
   * @param printer the printer used to convert the value to a {@link String}
   * @return this
   */
  public ApprovalBuilder<String> printWith(Function<T, String> printer) {
    return new ApprovalBuilder<>(printer.apply(value));
  }

  /**
   * Applies the given scrubber to the current value.
   *
   * @param scrubber the {@link UnaryOperator} or {@link Scrubber}
   * @return this
   */
  public ApprovalBuilder<T> scrubbedOf(UnaryOperator<T> scrubber) {
    value = scrubber.apply(value);
    return this;
  }

  /**
   * Uses the given {@link Consumer} or {@link Verifier} to approve the printed {@link #value}.
   *
   * @param verifier the {@link Consumer} or {@link Verifier}
   * @throws ApprovalError if the verification fails
   */
  public void verify(final Consumer<String> verifier) {
    if (value instanceof String printedValue) {
      verifier.accept(printedValue);
    } else {
      verifier.accept(DEFAULT_PRINTER.apply(value));
    }
  }

  /**
   * Uses the DEFAULT_VERIFIER to approve the printed {@link #value}.
   *
   * @throws ApprovalError if the verification fails
   */
  public void verify() {
    verify(file());
  }
}
