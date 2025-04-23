package org.approvej;

import static org.approvej.verify.PathProvider.DEFAULT_FILENAME_EXTENSION;
import static org.approvej.verify.PathProviders.nextToTest;
import static org.approvej.verify.Verifiers.file;
import static org.approvej.verify.Verifiers.value;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.approvej.print.Printer;
import org.approvej.scrub.Scrubber;
import org.approvej.verify.InplaceVerifier;
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

  private T receivedValue;
  private final String filenameExtension;

  private ApprovalBuilder(T originalValue, String filenameExtension) {
    this.receivedValue = originalValue;
    this.filenameExtension = filenameExtension;
  }

  /**
   * Creates a new builder for the given value.
   *
   * @param originalValue the value to approve
   * @return a new {@link ApprovalBuilder} for the given value
   * @param <T> the type of the value to approve
   */
  public static <T> ApprovalBuilder<T> approve(T originalValue) {
    return new ApprovalBuilder<>(originalValue, DEFAULT_FILENAME_EXTENSION);
  }

  /**
   * Uses the given {@link Function} to convert the {@link #receivedValue} to a {@link String}.
   *
   * @param printer the {@link Function} used to convert the {@link #receivedValue} to a {@link
   *     String}
   * @return this
   */
  public ApprovalBuilder<String> printWith(Function<T, String> printer) {
    return new ApprovalBuilder<>(printer.apply(receivedValue), DEFAULT_FILENAME_EXTENSION);
  }

  /**
   * Uses the given {@link Printer} to convert the {@link #receivedValue} to a {@link String}.
   *
   * @param printer the printer used to convert the value to a {@link String}
   * @return this
   */
  public ApprovalBuilder<String> printWith(Printer<T> printer) {
    return new ApprovalBuilder<>(printer.apply(receivedValue), printer.filenameExtension());
  }

  /**
   * Applies the given scrubber to the current {@link #receivedValue}.
   *
   * @param scrubber the {@link UnaryOperator} or {@link Scrubber}
   * @return this
   */
  public ApprovalBuilder<T> scrubbedOf(UnaryOperator<T> scrubber) {
    receivedValue = scrubber.apply(receivedValue);
    return this;
  }

  /**
   * Uses the given {@link Consumer} or {@link Verifier} to approve the printed {@link
   * #receivedValue}.
   *
   * @param verifier the {@link Consumer} or {@link Verifier}
   * @throws ApprovalError if the verification fails
   */
  public void by(final Consumer<String> verifier) {
    if (receivedValue instanceof String printedValue) {
      verifier.accept(printedValue);
    } else {
      verifier.accept(DEFAULT_PRINTER.apply(receivedValue));
    }
  }

  /**
   * Verifies that the given previouslyApproved value equals the {@link #receivedValue} using an
   * {@link InplaceVerifier}.
   *
   * @param previouslyApproved the approved value
   */
  public void byValue(final String previouslyApproved) {
    by(value(previouslyApproved));
  }

  /**
   * Uses a {@link org.approvej.verify.FileVerifier} to approve the printed {@link #receivedValue}.
   *
   * @throws ApprovalError if the verification fails
   */
  public void byFile() {
    by(file(nextToTest().filenameExtension(filenameExtension)));
  }
}
