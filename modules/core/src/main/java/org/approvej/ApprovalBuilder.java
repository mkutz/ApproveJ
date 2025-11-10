package org.approvej;

import static org.approvej.Configuration.configuration;
import static org.approvej.approve.Approvers.file;
import static org.approvej.approve.Approvers.value;
import static org.approvej.approve.PathProviders.approvedPath;
import static org.approvej.approve.PathProviders.nextToTest;
import static org.approvej.print.Printer.DEFAULT_FILENAME_EXTENSION;
import static org.approvej.review.FileReviewerScript.script;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.approvej.approve.Approver;
import org.approvej.approve.FileApprover;
import org.approvej.approve.InplaceApprover;
import org.approvej.approve.PathProvider;
import org.approvej.approve.PathProviders;
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
 * method {@link #printedBy(Printer)} to customize that. By default, the value's {@link
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
 * <p>The builder is concluded by specifying an approver to approve the value {@link #by(Function)}
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

  private final T value;
  private final String name;
  private final String filenameExtension;
  @Nullable private final FileReviewer fileReviewer;

  private ApprovalBuilder(
      T value, String name, String filenameExtension, @Nullable FileReviewer fileReviewer) {
    this.value = value;
    this.name = name;
    this.filenameExtension = filenameExtension;
    this.fileReviewer = fileReviewer;
  }

  /**
   * Creates a new builder for the given value.
   *
   * @param value the value to approve
   * @return a new {@link ApprovalBuilder} for the given value
   * @param <T> the type of the value to approve
   */
  public static <T> ApprovalBuilder<T> approve(T value) {
    return new ApprovalBuilder<>(
        value, "", DEFAULT_FILENAME_EXTENSION, configuration.defaultFileReviewer());
  }

  /**
   * Sets a name for the current approval. Generally this should be used when there are multiple
   * values in one test case are being approved {@link #byFile()} as the second approval would
   * otherwise simply overwrite the first one.
   *
   * @param name the name for the current approval
   * @return a copy of this with the given {@link #name}
   */
  public ApprovalBuilder<T> named(String name) {
    return new ApprovalBuilder<>(value, name, filenameExtension, fileReviewer);
  }

  /**
   * Uses the given {@link Function} to convert the {@link #value} to a {@link String}.
   *
   * @param printer the {@link Function} used to convert the {@link #value} to a {@link String}
   * @return a copy of this with the printed {@link #value}
   */
  public ApprovalBuilder<String> printedBy(Function<T, String> printer) {
    return new ApprovalBuilder<>(printer.apply(value), name, filenameExtension, fileReviewer);
  }

  /**
   * Uses the given {@link Function} to convert the {@link #value} to a {@link String}.
   *
   * @param printer the {@link Function} used to convert the {@link #value} to a {@link String}
   * @return a copy of this with the printed {@link #value}
   * @deprecated use {@link #printedBy(Function)}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public ApprovalBuilder<String> printWith(Function<T, String> printer) {
    return printedBy(printer);
  }

  /**
   * Uses the given {@link Printer} to convert the {@link #value} to a {@link String}.
   *
   * @param printer the printer used to convert the value to a {@link String}
   * @return a copy of this with the printed {@link #value}
   */
  public ApprovalBuilder<String> printedBy(Printer<T> printer) {
    return new ApprovalBuilder<>(
        printer.apply(value), name, printer.filenameExtension(), fileReviewer);
  }

  /**
   * Uses the given {@link Printer} to convert the {@link #value} to a {@link String}.
   *
   * @param printer the printer used to convert the value to a {@link String}
   * @return a copy of this with the printed {@link #value}
   * @deprecated use {@link #printedBy(Printer)}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public ApprovalBuilder<String> printWith(Printer<T> printer) {
    return printedBy(printer);
  }

  /**
   * Uses the default {@link Printer} to convert the {@link #value} to a {@link String}.
   *
   * @return a copy of this with the printed {@link #value}
   * @see Configuration#defaultPrinter()
   * @see #printedBy(Printer)
   */
  public ApprovalBuilder<String> printed() {
    // noinspection unchecked
    return printedBy((Printer<T>) configuration.defaultPrinter());
  }

  /**
   * Uses the default {@link Printer} to convert the {@link #value} to a {@link String}.
   *
   * @return a copy of this with the printed {@link #value}
   * @see Configuration#defaultPrinter()
   * @see #printedBy(Printer)
   * @deprecated use {@link #printed()}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public ApprovalBuilder<String> print() {
    return printed();
  }

  /**
   * Applies the given scrubber to the current {@link #value}.
   *
   * @param scrubber the {@link UnaryOperator} or {@link Scrubber}
   * @return a copy of this with the scrubbed {@link #value}
   */
  public ApprovalBuilder<T> scrubbedOf(UnaryOperator<T> scrubber) {
    return new ApprovalBuilder<>(scrubber.apply(value), name, filenameExtension, fileReviewer);
  }

  /**
   * Sets the given {@link FileReviewer} to trigger if the received value is not equal to the
   * previously approved.
   *
   * @param fileReviewer the {@link FileReviewer} to be used
   * @return a copy of this with the given {@link #fileReviewer}
   * @see Configuration#defaultFileReviewer()
   * @see org.approvej.review.FileReviewerScript#script()
   */
  public ApprovalBuilder<T> reviewedBy(FileReviewer fileReviewer) {
    return new ApprovalBuilder<>(value, name, filenameExtension, fileReviewer);
  }

  /**
   * Sets the given {@link FileReviewer} to trigger if the received value is not equal to the
   * previously approved.
   *
   * @param fileReviewer the {@link FileReviewer} to be used
   * @return a copy of this with the given {@link #fileReviewer}
   * @see Configuration#defaultFileReviewer()
   * @see org.approvej.review.FileReviewerScript#script()
   * @deprecated use {@link #reviewedBy(String)} (Function)}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public ApprovalBuilder<T> reviewWith(FileReviewer fileReviewer) {
    return reviewedBy(fileReviewer);
  }

  /**
   * Creates a {@link org.approvej.review.FileReviewerScript} from the given script {@link String}
   * to trigger if the received value is not equal to the previously approved.
   *
   * @param script the script {@link String} to be used as a {@link
   *     org.approvej.review.FileReviewerScript}
   * @return a copy of this with the given script as {@link #fileReviewer}
   * @see Configuration#defaultFileReviewer()
   * @see org.approvej.review.FileReviewerScript#script()
   */
  public ApprovalBuilder<T> reviewedBy(String script) {
    return reviewedBy(script(script));
  }

  /**
   * Creates a {@link org.approvej.review.FileReviewerScript} from the given script {@link String}
   * to trigger if the received value is not equal to the previously approved.
   *
   * @param script the script {@link String} to be used as a {@link
   *     org.approvej.review.FileReviewerScript}
   * @return a copy of this with the given script as {@link #fileReviewer}
   * @see Configuration#defaultFileReviewer()
   * @see org.approvej.review.FileReviewerScript#script()
   * @deprecated use {@link #reviewedBy(String)}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public ApprovalBuilder<T> reviewWith(String script) {
    return reviewedBy(script(script));
  }

  /**
   * Approves the {@link #value} by the given approver.
   *
   * <p>If necessary the {@link #value} is printed using the {@link Configuration#defaultPrinter()}.
   *
   * @param approver a {@link Function} or an {@link Approver} implementation
   * @throws ApprovalError if the approval fails
   */
  public void by(final Function<String, ApprovalResult> approver) {
    if (!(value instanceof String)) {
      printed().by(approver);
    }
    ApprovalResult result = approver.apply(String.valueOf(value));
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
    PathProvider updatedPathProvider =
        pathProvider.filenameAffix(name).filenameExtension(filenameExtension);
    if (!(value instanceof String)) {
      printed().byFile(updatedPathProvider);
    }
    FileApprover approver = file(updatedPathProvider);
    ApprovalResult approvalResult = approver.apply(String.valueOf(value));
    if (approvalResult.needsApproval() && fileReviewer != null) {
      ReviewResult reviewResult = fileReviewer.apply(updatedPathProvider);
      if (reviewResult.needsReapproval()) {
        approvalResult = approver.apply(String.valueOf(value));
      }
    }
    approvalResult.throwIfNotApproved();
  }

  /**
   * Approves the receivedValue by a {@link FileApprover}, using a {@link PathProviders#nextToTest()
   * nextToTest PathProviderBuilder}, and the {@link Printer#filenameExtension() used Printer's
   * filenameExtension}.
   *
   * @throws ApprovalError if the approval fails
   */
  public void byFile() {
    byFile(nextToTest());
  }

  /**
   * Approves the receivedValue by a {@link FileApprover} with an {@link
   * PathProviders#approvedPath(Path)} with the given {@link Path} to the approved file.
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
   * PathProviders#approvedPath(Path)} with the given path to the approved file.
   *
   * <p>Note: the {@link Printer}'s filenameExtension is ignored.
   *
   * @param approvedPath the path to the approved file
   * @throws ApprovalError if the approval fails
   */
  public void byFile(String approvedPath) {
    byFile(Path.of(approvedPath));
  }
}
