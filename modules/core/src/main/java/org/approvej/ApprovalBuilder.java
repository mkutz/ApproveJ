package org.approvej;

import static org.approvej.Configuration.configuration;
import static org.approvej.approve.Approvers.file;
import static org.approvej.approve.Approvers.value;
import static org.approvej.approve.PathProvider.DEFAULT_FILENAME_EXTENSION;
import static org.approvej.approve.PathProviders.approvedPath;
import static org.approvej.approve.PathProviders.nextToTest;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.approvej.approve.ApprovedPathProvider;
import org.approvej.approve.Approver;
import org.approvej.approve.FileApprover;
import org.approvej.approve.InplaceApprover;
import org.approvej.approve.PathProvider;
import org.approvej.print.Printer;
import org.approvej.scrub.Scrubber;
import org.jspecify.annotations.NullMarked;

/**
 * A builder to configure an approval for a given value.
 *
 * <p>E.g. {@code approve(result).byFile();} will approve the result with the content of a file next
 * to the test.
 *
 * <h2>Printing</h2>
 *
 * <p>Before approval, the value needs to be printed (turned into a {@link String}). You can use the
 * method {@link #printWith(Printer)} to customize that. By default, the value's {@link
 * Object#toString() toString method} will be called.
 *
 * <p>E.g. {@code approve(result).printWith(objectPrinter()).byFile();} prints the given object
 * using the given {@link org.approvej.print.ObjectPrinter}.
 *
 * <h2>Scrubbing</h2>
 *
 * <p>The value can also be {@link #scrubbedOf(UnaryOperator) scrubbed of} dynamic data (like
 * timestamps or ID's).
 *
 * <p>E.g. {@code approve(result).scrubbedOf(uuids()).byFile();} will replace all UUID's in the
 * result before approval.
 *
 * <h2>Approving</h2>
 *
 * <p>The builder is concluded by specifying an approver to approve the value {@link #by(Consumer)
 * by} ( {@link #byFile()} and {@link #byValue(String)}).
 *
 * <p>E.g. {@code approve(result).byFile();} approves the result with the content of a file next to
 * the test, while {@code approve(result).byValue(approved);} approves the result with the given
 * approved value.
 *
 * @param <T> the type of the value to approve
 */
@NullMarked
public class ApprovalBuilder<T> {

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
   * Approves the {@link #receivedValue} by the given approver.
   *
   * <p>If necessary the {@link #receivedValue} is printed using the {@link
   * Configuration#defaultPrinter()}.
   *
   * @param approver a {@link Consumer} or an {@link Approver} implementation
   * @throws ApprovalError if the approval fails
   */
  public void by(final Consumer<String> approver) {
    if (receivedValue instanceof String printedValue) {
      approver.accept(printedValue);
    } else {
      approver.accept(configuration.defaultPrinter().apply(receivedValue));
    }
  }

  /**
   * Approves the value by an {@link InplaceApprover} with the given previouslyApproved value.
   *
   * @param previouslyApproved the approved value
   */
  public void byValue(final String previouslyApproved) {
    by(value(previouslyApproved));
  }

  /**
   * Approves the receivedValue by a {@link FileApprover}, a {@link
   * org.approvej.approve.NextToTestPathProvider}, and the {@link Printer#filenameExtension()}.
   *
   * @throws ApprovalError if the approval fails
   */
  public void byFile() {
    by(file(nextToTest().filenameExtension(filenameExtension)));
  }

  /**
   * Approves the receivedValue by a {@link FileApprover} with the given {@link PathProvider}.
   *
   * @param pathProvider the provider for the paths of the approved and received files
   * @throws ApprovalError if the approval fails
   */
  public void byFile(PathProvider pathProvider) {
    by(file(pathProvider));
  }

  /**
   * Approves the receivedValue by a {@link FileApprover} with an {@link ApprovedPathProvider} and
   * the given {@link Path} to the approved file.
   *
   * @param approvedPath the {@link Path} to the approved file
   * @throws ApprovalError if the approval fails
   */
  public void byFile(Path approvedPath) {
    by(file(approvedPath(approvedPath)));
  }

  /**
   * Approves the receivedValue by a {@link FileApprover} with an {@link ApprovedPathProvider} and
   * the given path to the approved file.
   *
   * @param approvedPath the path to the approved file
   * @throws ApprovalError if the approval fails
   */
  public void byFile(String approvedPath) {
    by(file(approvedPath(Path.of(approvedPath))));
  }
}
