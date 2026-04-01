package org.approvej;

import static org.approvej.approve.Approvers.file;
import static org.approvej.approve.Approvers.value;
import static org.approvej.approve.PathProviders.approvedPath;
import static org.approvej.approve.PathProviders.nextToTest;
import static org.approvej.configuration.Configuration.configuration;
import static org.approvej.print.PrintFormat.DEFAULT_FILENAME_EXTENSION;
import static org.approvej.review.Reviewers.script;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;
import org.approvej.approve.ApprovedFileInventoryUpdater;
import org.approvej.approve.Approver;
import org.approvej.approve.InlineValueRewriter;
import org.approvej.approve.PathProvider;
import org.approvej.approve.PathProviders;
import org.approvej.approve.StackTraceTestFinderUtil;
import org.approvej.print.PrintFormat;
import org.approvej.print.Printer;
import org.approvej.review.NoneReviewer;
import org.approvej.review.ReviewResult;
import org.approvej.review.Reviewer;
import org.approvej.scrub.Scrubber;
import org.jspecify.annotations.NullMarked;
import org.opentest4j.TestAbortedException;

/**
 * A builder to configure an approval for a given value.
 *
 * <p>E.g. {@code approve(result).byFile();} will approve the result with the content of a file next
 * to the test.
 *
 * <h2>Printing</h2>
 *
 * <p>Before approval, the value needs to be printed (turned into a {@link String}). You can use the
 * methods {@link #printedAs(PrintFormat)} or {@link #printedBy(Function)} to customize that. By
 * default, the value's {@link Object#toString() toString method} will be called.
 *
 * <p>E.g. {@code approve(result).printAs(multiLineString()).byFile();} prints the given object
 * using the given {@link org.approvej.print.MultiLineStringPrintFormat}.
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

  private static final Logger LOGGER = Logger.getLogger(ApprovalBuilder.class.getName());

  private final T value;
  private final String name;
  private final String filenameExtension;
  private final Reviewer fileReviewer;
  private final AtomicBoolean concluded;

  private ApprovalBuilder(
      T value,
      String name,
      String filenameExtension,
      Reviewer fileReviewer,
      AtomicBoolean concluded) {
    this.value = value;
    this.name = name;
    this.filenameExtension = filenameExtension;
    this.fileReviewer = fileReviewer;
    this.concluded = concluded;
  }

  /**
   * Creates a new builder for the given value.
   *
   * @param value the value to approve
   * @return a new {@link ApprovalBuilder} for the given value
   * @param <T> the type of the value to approve
   */
  public static <T> ApprovalBuilder<T> approve(T value) {
    AtomicBoolean concluded = DanglingApprovalTracker.register();
    return new ApprovalBuilder<>(
        value, "", DEFAULT_FILENAME_EXTENSION, configuration.defaultFileReviewer(), concluded);
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
    return new ApprovalBuilder<>(value, name, filenameExtension, fileReviewer, concluded);
  }

  /**
   * Uses the given {@link Function} to convert the {@link #value} to a {@link String}.
   *
   * @param printer the {@link Function} used to convert the {@link #value} to a {@link String}
   * @return a copy of this with the printed {@link #value}
   */
  public ApprovalBuilder<String> printedBy(Function<? super T, String> printer) {
    return new ApprovalBuilder<>(
        printer.apply(value), name, filenameExtension, fileReviewer, concluded);
  }

  /**
   * Uses the given {@link Printer} to convert the {@link #value} to a {@link String}.
   *
   * @param printFormat the printer used to convert the value to a {@link String}
   * @return a copy of this with the printed {@link #value}
   */
  public ApprovalBuilder<String> printedAs(PrintFormat<? super T> printFormat) {
    return new ApprovalBuilder<>(
        printFormat.printer().apply(value),
        name,
        printFormat.filenameExtension(),
        fileReviewer,
        concluded);
  }

  /**
   * Uses the default {@link PrintFormat} to convert the {@link #value} to a {@link String}.
   *
   * @return a copy of this with the printed {@link #value}
   * @see org.approvej.configuration.Configuration#defaultPrintFormat()
   * @see #printedAs(PrintFormat)
   */
  public ApprovalBuilder<String> printed() {
    return printedAs(configuration.defaultPrintFormat());
  }

  /**
   * Applies the given scrubber to the current {@link #value}.
   *
   * @param scrubber the {@link UnaryOperator} or {@link Scrubber}
   * @return a copy of this with the scrubbed {@link #value}
   */
  public ApprovalBuilder<T> scrubbedOf(UnaryOperator<T> scrubber) {
    return new ApprovalBuilder<>(
        scrubber.apply(value), name, filenameExtension, fileReviewer, concluded);
  }

  /**
   * Sets the given {@link Reviewer} to trigger if the received value is not equal to the previously
   * approved.
   *
   * @param fileReviewer the {@link Reviewer} to be used
   * @return a copy of this with the given {@link #fileReviewer}
   * @see org.approvej.configuration.Configuration#defaultFileReviewer()
   * @see org.approvej.review.Reviewers
   */
  public ApprovalBuilder<T> reviewedBy(Reviewer fileReviewer) {
    return new ApprovalBuilder<>(value, name, filenameExtension, fileReviewer, concluded);
  }

  /**
   * Creates a {@link Reviewer} from the given script {@link String} to trigger if the received
   * value is not equal to the previously approved.
   *
   * @param script the script {@link String} to be used as a {@link
   *     org.approvej.review.Reviewers#script(String) script}
   * @return a copy of this with the given script as {@link #fileReviewer}
   * @see org.approvej.configuration.Configuration#defaultFileReviewer()
   * @see org.approvej.review.Reviewers#script(String)
   */
  public ApprovalBuilder<T> reviewedBy(String script) {
    return reviewedBy(script(script));
  }

  /**
   * Approves the {@link #value} by the given approver.
   *
   * <p>If necessary the {@link #value} is printed using the {@link
   * org.approvej.configuration.Configuration#defaultPrintFormat()}.
   *
   * @param approver a {@link Function} or an {@link Approver} implementation
   * @throws ApprovalError if the approval fails
   */
  public void by(final Function<String, ApprovalResult> approver) {
    concluded.set(true);
    if (!(value instanceof String)) {
      printed().by(approver);
      return;
    }
    ApprovalResult result = approver.apply(String.valueOf(value));
    if (result.needsApproval()) {
      throw new ApprovalError(result.received(), result.previouslyApproved());
    }
  }

  /**
   * Approves the value by the given previouslyApproved value.
   *
   * <p>When the {@link org.approvej.configuration.Configuration#defaultInlineValueReviewer()} is
   * configured (e.g. as {@code automatic}), a mismatch will trigger the reviewer. The {@code
   * automatic} reviewer rewrites the string literal in the test source file with the received value
   * and aborts the test with a {@link TestAbortedException} so the developer can verify the change
   * and re-run. The test is aborted rather than failed because the JVM is still running the old
   * bytecode and a re-run is always necessary.
   *
   * @param previouslyApproved the approved value
   */
  public void byValue(final String previouslyApproved) {
    concluded.set(true);
    if (!(value instanceof String)) {
      printed().byValue(previouslyApproved);
      return;
    }
    Approver approver = value(previouslyApproved);
    ApprovalResult result = approver.apply(String.valueOf(value));
    if (result.needsApproval()) {
      reviewInlineValue(String.valueOf(value).trim());
      throw new ApprovalError(result.received(), result.previouslyApproved());
    }
  }

  private void reviewInlineValue(String received) {
    Reviewer inlineValueReviewer = configuration.defaultInlineValueReviewer();
    if (inlineValueReviewer instanceof NoneReviewer) {
      return;
    }
    try {
      Method testMethod = StackTraceTestFinderUtil.currentTestMethod().method();
      Path sourcePath = StackTraceTestFinderUtil.findTestSourcePath(testMethod);
      PathProvider pathProvider =
          new PathProvider(sourcePath.getParent(), sourcePath.getFileName().toString(), "", "", "");
      String content = Files.readString(sourcePath);
      String rewritten =
          InlineValueRewriter.rewriteContent(content, testMethod.getName(), received, sourcePath);
      Files.writeString(pathProvider.receivedPath(), rewritten);
      ReviewResult reviewResult = inlineValueReviewer.apply(pathProvider);
      Files.deleteIfExists(pathProvider.receivedPath());
      if (reviewResult.needsReapproval()) {
        String updatedContent = Files.readString(sourcePath);
        if (!updatedContent.equals(content)) {
          throw new TestAbortedException(
              "Inline value updated in source file. Re-run the test to verify.");
        }
      }
    } catch (TestAbortedException aborted) {
      throw aborted;
    } catch (RuntimeException | IOException error) {
      LOGGER.warning("Could not auto-update inline value: " + error.getMessage());
    }
  }

  /**
   * Approves the receivedValue by a file defined by the given {@link PathProvider}.
   *
   * @param pathProvider the provider for the paths of the approved and received files
   * @throws ApprovalError if the approval fails
   */
  public void byFile(PathProvider pathProvider) {
    concluded.set(true);
    PathProvider updatedPathProvider =
        pathProvider.filenameAffix(name).filenameExtension(filenameExtension);
    if (configuration.inventoryEnabled()) {
      ApprovedFileInventoryUpdater.registerApprovedFile(updatedPathProvider);
    }
    if (!(value instanceof String)) {
      printed().byFile(updatedPathProvider);
      return;
    }
    Approver approver = file(updatedPathProvider);
    ApprovalResult approvalResult = approver.apply(String.valueOf(value));
    if (approvalResult.needsApproval()) {
      ReviewResult reviewResult = fileReviewer.apply(updatedPathProvider);
      if (reviewResult.needsReapproval()) {
        approvalResult = approver.apply(String.valueOf(value));
      }
    }
    approvalResult.throwIfNotApproved();
  }

  /**
   * Approves the receivedValue by a file, using a {@link PathProviders#nextToTest() nextToTest
   * PathProvider}, and the {@link PrintFormat#filenameExtension() used PrintFormat's
   * filenameExtension}.
   *
   * @throws ApprovalError if the approval fails
   */
  public void byFile() {
    byFile(nextToTest());
  }

  /**
   * Approves the receivedValue by a file with an {@link PathProviders#approvedPath(Path)} with the
   * given {@link Path} to the approved file.
   *
   * <p>Note: the {@link PrintFormat}'s filenameExtension is ignored.
   *
   * @param approvedPath the {@link Path} to the approved file
   * @throws ApprovalError if the approval fails
   */
  public void byFile(Path approvedPath) {
    byFile(approvedPath(approvedPath));
  }

  /**
   * Approves the receivedValue by a {@link Approver} with an {@link
   * PathProviders#approvedPath(Path)} with the given path {@link String} to the approved file.
   *
   * <p>Note: the {@link PrintFormat}'s filenameExtension is ignored.
   *
   * @param approvedPath the path to the approved file
   * @throws ApprovalError if the approval fails
   */
  public void byFile(String approvedPath) {
    byFile(Path.of(approvedPath));
  }
}
