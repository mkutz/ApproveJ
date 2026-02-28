package org.approvej;

import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * A JUnit Jupiter extension that detects dangling approvals after each test.
 *
 * <p>A dangling approval occurs when {@link ApprovalBuilder#approve(Object) approve()} is called
 * without a concluding terminal method ({@link ApprovalBuilder#by(java.util.function.Function)
 * by()}, {@link ApprovalBuilder#byFile() byFile()}, or {@link ApprovalBuilder#byValue(String)
 * byValue()}).
 *
 * <p>Use the {@link ApprovalTest} annotation on test classes to enable this extension, or register
 * it directly via {@code @ExtendWith(DanglingApprovalExtension.class)}.
 *
 * @see ApprovalTest
 */
@NullMarked
public class DanglingApprovalExtension implements AfterEachCallback {

  @Override
  public void afterEach(ExtensionContext context) {
    DanglingApprovalTracker.checkAndReset();
  }
}
