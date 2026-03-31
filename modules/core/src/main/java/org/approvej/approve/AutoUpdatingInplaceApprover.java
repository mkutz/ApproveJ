package org.approvej.approve;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.logging.Logger;
import org.approvej.ApprovalError;
import org.approvej.ApprovalResult;
import org.jspecify.annotations.NullMarked;

/**
 * An {@link Approver} that checks if the received value matches the previously approved value and
 * automatically rewrites the test source file on mismatch.
 *
 * <p>When the values differ, this approver locates the test source file, rewrites the {@code
 * byValue()} argument with the received value, and throws an {@link ApprovalError} prompting the
 * developer to re-run the test.
 *
 * @param previouslyApproved the approved value
 */
@NullMarked
record AutoUpdatingInplaceApprover(String previouslyApproved) implements Approver {

  private static final Logger LOGGER =
      Logger.getLogger(AutoUpdatingInplaceApprover.class.getName());

  AutoUpdatingInplaceApprover(String previouslyApproved) {
    this.previouslyApproved = previouslyApproved.trim();
  }

  @Override
  public ApprovalResult apply(String received) {
    String trimmedReceived = received.trim();
    if (trimmedReceived.equals(previouslyApproved)) {
      return new InplaceApprovalResult(trimmedReceived, previouslyApproved);
    }
    try {
      Method testMethod = StackTraceTestFinderUtil.currentTestMethod().method();
      Path sourcePath = StackTraceTestFinderUtil.findTestSourcePath(testMethod);
      InlineValueRewriter.rewrite(sourcePath, testMethod.getName(), trimmedReceived);
      throw new ApprovalError("Inline value updated. Re-run the test.");
    } catch (ApprovalError approvalError) {
      throw approvalError;
    } catch (RuntimeException error) {
      LOGGER.warning("Could not auto-update inline value: " + error.getMessage());
    }
    return new InplaceApprovalResult(trimmedReceived, previouslyApproved);
  }
}
