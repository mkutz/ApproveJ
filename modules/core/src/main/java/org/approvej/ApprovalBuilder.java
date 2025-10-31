package org.approvej;

import static org.approvej.Configuration.configuration;
import static org.approvej.approve.Approvers.file;
import static org.approvej.approve.Approvers.value;
import static org.approvej.approve.PathProviderBuilder.approvedPath;
import static org.approvej.print.Printer.DEFAULT_FILENAME_EXTENSION;
import static org.approvej.review.FileReviewerScript.script;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.approvej.approve.Approver;
import org.approvej.approve.FileApprover;
import org.approvej.approve.InplaceApprover;
import org.approvej.approve.PathProvider;
import org.approvej.approve.PathProviderBuilder;
import org.approvej.print.MultiLineStringPrinter;
import org.approvej.print.Printer;
import org.approvej.review.FileReviewer;
import org.approvej.review.ReviewResult;
import org.approvej.scrub.Scrubber;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A builder to configure an approval for a given value.
 *
 * <p>E.g. {@code approve(result).byFile();} will approve the result with the content of a file next
 * to the test.
 *
 * <h2>Printing</h2>
 *
 * <p>Before approval, the value needs to be printed (turned into a {@link String}). You can use the
 * method {@link #printedAs(Printer)} to customize that. By default, the value's {@link
 * Object#toString() toString method} will be called.
 *
 * <p>E.g. {@code approve(result).printedAs(multiLineString()).byFile();} prints the given object
 * using the given {@link MultiLineStringPrinter}.
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
 * <p>The builder is concluded by specifying an approver to approve the value {@link #by(Function)
 * by} ({@link #byFile()} and {@link #byValue(String)}).
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
  private String name;
  private final String filenameExtension;
  @Nullable private FileReviewer fileReviewer;

  private ApprovalBuilder(T originalValue, String name, String filenameExtension) {
    this.receivedValue = originalValue;
    this.name = name;
    this.filenameExtension = filenameExtension;
    this.fileReviewer = configuration.defaultFileReviewer();
  }

  /**
   * Creates a new builder for the given value.
   *
   * @param originalValue the value to approve
   * @return a new {@link ApprovalBuilder} for the given value
   * @param <T> the type of the value to approve
   */
  public static <T> ApprovalBuilder<T> approve(T originalValue) {
    return new ApprovalBuilder<>(originalValue, "", DEFAULT_FILENAME_EXTENSION);
  }

  /**
   * Sets a name for the current approval. Generally this should be used when there are multiple
   * values in one test case are being approved {@link #byFile()} as the second approval would
   * otherwise simply overwrite the first one.
   *
   * @param name the name for the current approval
   * @return this
   */
  public ApprovalBuilder<T> named(String name) {
    this.name = name;
    return this;
  }

  /**
   * Uses the given {@link Function} to convert the {@link #receivedValue} to a {@link String}.
   *
   * @param printer the {@link Function} used to convert the {@link #receivedValue} to a {@link
   *     String}
   * @return a new {@link ApprovalBuilder} with the printed value
   */
  public ApprovalBuilder<String> printedWith(Function<? super T, String> printer) {
    return new ApprovalBuilder<>(printer.apply(receivedValue), name, DEFAULT_FILENAME_EXTENSION);
  }

  /**
   * Uses the given {@link Function} to convert the {@link #receivedValue} to a {@link String}.
   *
   * @param printer the {@link Function} used to convert the {@link #receivedValue} to a {@link
   *     String}
   * @return a new {@link ApprovalBuilder} with the printed value
   * @deprecated use {@link #printedWith(Function)}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public ApprovalBuilder<String> printWith(Function<? super T, String> printer) {
    return printedWith(printer);
  }

  /**
   * Uses the given {@link Printer} to convert the {@link #receivedValue} to a {@link String}.
   *
   * @param printer the printer used to convert the value to a {@link String}
   * @return a new {@link ApprovalBuilder} with the printed value
   */
  public ApprovalBuilder<String> printedAs(Printer<? super T> printer) {
    return new ApprovalBuilder<>(printer.apply(receivedValue), name, printer.filenameExtension());
  }

  /**
   * Uses the given {@link Printer} to convert the {@link #receivedValue} to a {@link String}.
   *
   * @param printer the printer used to convert the value to a {@link String}
   * @return a new {@link ApprovalBuilder} with the printed value
   * @deprecated use {@link #printedAs(Printer)}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public ApprovalBuilder<String> printWith(Printer<? super T> printer) {
    return printedAs(printer);
  }

  /**
   * Uses the default {@link Printer} to convert the {@link #receivedValue} to a {@link String}.
   *
   * @return a new {@link ApprovalBuilder} with the printed value
   * @see Configuration#defaultPrinter()
   * @see #printedAs(Printer)
   */
  public ApprovalBuilder<String> printed() {
    return printedAs(configuration.defaultPrinter());
  }

  /**
   * Uses the default {@link Printer} to convert the {@link #receivedValue} to a {@link String}.
   *
   * @return a new {@link ApprovalBuilder} with the printed value
   * @see Configuration#defaultPrinter()
   * @see #printedAs(Printer)
   * @deprecated use {@link #printed()}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public ApprovalBuilder<String> print() {
    return printed();
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
   * Sets the given {@link FileReviewer} to trigger if the received value is not equal to the
   * previously approved.
   *
   * @param reviewer the {@link FileReviewer} to be used
   * @return this
   * @see Configuration#defaultFileReviewer()
   * @see org.approvej.review.FileReviewerScript#script()
   */
  public ApprovalBuilder<T> reviewedWith(FileReviewer reviewer) {
    this.fileReviewer = reviewer;
    return this;
  }

  /**
   * Sets the given {@link FileReviewer} to trigger if the received value is not equal to the
   * previously approved.
   *
   * @param reviewer the {@link FileReviewer} to be used
   * @return this
   * @see Configuration#defaultFileReviewer()
   * @see org.approvej.review.FileReviewerScript#script()
   * @deprecated use {@link #reviewedWith(FileReviewer)}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public ApprovalBuilder<T> reviewWith(FileReviewer reviewer) {
    return reviewedWith(reviewer);
  }

  /**
   * Creates a {@link org.approvej.review.FileReviewerScript} from the given script {@link String}
   * to trigger if the received value is not equal to the previously approved.
   *
   * @param script the script {@link String} to be used as a {@link
   *     org.approvej.review.FileReviewerScript}
   * @return this
   * @see Configuration#defaultFileReviewer()
   * @see org.approvej.review.FileReviewerScript#script()
   */
  public ApprovalBuilder<T> reviewedWith(String script) {
    return reviewedWith(script(script));
  }

  /**
   * Creates a {@link org.approvej.review.FileReviewerScript} from the given script {@link String}
   * to trigger if the received value is not equal to the previously approved.
   *
   * @param script the script {@link String} to be used as a {@link
   *     org.approvej.review.FileReviewerScript}
   * @return this
   * @see Configuration#defaultFileReviewer()
   * @see org.approvej.review.FileReviewerScript#script()
   * @deprecated use {@link #reviewedWith(String)}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public ApprovalBuilder<T> reviewWith(String script) {
    return reviewedWith(script);
  }

  /**
   * Approves the {@link #receivedValue} by the given approver.
   *
   * <p>If necessary the {@link #receivedValue} is printed using the {@link
   * Configuration#defaultPrinter()}.
   *
   * @param approver a {@link Function} or an {@link Approver} implementation
   * @throws ApprovalError if the approval fails
   */
  public void by(final Function<String, ApprovalResult> approver) {
    if (!(receivedValue instanceof String)) {
      printed().by(approver);
    }
    ApprovalResult result = approver.apply(String.valueOf(receivedValue));
    if (result.needsApproval()) {
      throw new ApprovalError(result.received(), result.previouslyApproved());
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
   * Approves the receivedValue by a {@link FileApprover} with the given {@link PathProvider}.
   *
   * @param pathProvider the provider for the paths of the approved and received files
   * @throws ApprovalError if the approval fails
   */
  public void byFile(PathProvider pathProvider) {
    FileApprover approver = file(pathProvider);
    ApprovalResult approvalResult = approver.apply(String.valueOf(receivedValue));
    if (approvalResult.needsApproval() && fileReviewer != null) {
      ReviewResult reviewResult = fileReviewer.apply(pathProvider);
      if (reviewResult.needsReapproval()) {
        approvalResult = approver.apply(String.valueOf(receivedValue));
      }
    }
    approvalResult.throwIfNotApproved();
  }

  /**
   * Approves the receivedValue by a {@link FileApprover} with the given {@link
   * PathProviderBuilder}.
   *
   * @param pathProviderBuilder the provider for the paths of the approved and received files
   * @throws ApprovalError if the approval fails
   */
  public void byFile(PathProviderBuilder pathProviderBuilder) {
    if (!(receivedValue instanceof String)) {
      printed().byFile(pathProviderBuilder);
    } else {
      byFile(pathProviderBuilder.filenameAffix(name).filenameExtension(filenameExtension));
    }
  }

  /**
   * Approves the receivedValue by a {@link FileApprover}, using a {@link
   * PathProviderBuilder#nextToTest() nextToTest PathProviderBuilder}, and the {@link
   * Printer#filenameExtension() used Printer's filenameExtension}.
   *
   * @throws ApprovalError if the approval fails
   */
  public void byFile() {
    byFile(PathProviderBuilder.nextToTest());
  }

  /**
   * Approves the receivedValue by a {@link FileApprover} with an {@link
   * PathProviderBuilder#approvedPath(Path)} with the given {@link Path} to the approved file.
   *
   * <p>Note: the {@link Printer}'s filenameExtension is ignored.
   *
   * @param approvedPath the {@link Path} to the approved file
   * @throws ApprovalError if the approval fails
   */
  public void byFile(Path approvedPath) {
    byFile(approvedPath(approvedPath));
  }

  /**
   * Approves the receivedValue by a {@link FileApprover} with an {@link
   * PathProviderBuilder#approvedPath(Path)} with the given path to the approved file.
   *
   * <p>Note: the {@link Printer}'s filenameExtension is ignored.
   *
   * @param approvedPath the path to the approved file
   * @throws ApprovalError if the approval fails
   */
  public void byFile(String approvedPath) {
    byFile(approvedPath(Path.of(approvedPath)));
  }
}
