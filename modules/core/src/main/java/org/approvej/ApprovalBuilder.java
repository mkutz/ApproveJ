package org.approvej;

import org.approvej.print.Printer;
import org.approvej.print.ToStringPrinter;
import org.approvej.scrub.Scrubber;
import org.approvej.verify.Verifier;
import org.jspecify.annotations.NullMarked;

/**
 * A builder to configure an approval for a given value.
 *
 * <p>Optionally the value can be "scrubbed" of dynamic data (like timestamps or ids).
 *
 * <p>The value will be printed (converted to {@link String}) using the builder's {@link Printer}.
 * The default {@link ToStringPrinter} can be changed with the {@link #printWith(Printer)}.
 *
 * @param <T> the type of the value to approve
 */
@NullMarked
public class ApprovalBuilder<T> {

  private Printer<T> printer = new ToStringPrinter<>();
  private T scrubbedValue;

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
   * Creates a new builder for the given value.
   *
   * @param originalValue the value to approve
   */
  public ApprovalBuilder(T originalValue) {
    this.scrubbedValue = originalValue;
  }

  /**
   * Uses the given {@link Printer} to convert the value to a {@link String}.
   *
   * @param printer the printer used to convert the value to a {@link String}
   * @return this
   */
  public ApprovalBuilder<T> printWith(Printer<T> printer) {
    this.printer = printer;
    return this;
  }

  /**
   * Applies the given scrubber to the current value.
   *
   * @param scrubber the {@link Scrubber}
   * @return this
   */
  public ApprovalBuilder<T> scrubbedWith(Scrubber<T> scrubber) {
    scrubbedValue = scrubber.apply(scrubbedValue);
    return this;
  }

  /**
   * Uses the given {@link Verifier} to approve the {@link #scrubbedValue} printed using the {@link
   * #printer}.
   *
   * @param verifier the {@link Verifier}
   * @throws ApprovalError if the verification fails
   */
  public void verify(final Verifier verifier) {
    verifier.accept(printer.apply(scrubbedValue));
  }
}
