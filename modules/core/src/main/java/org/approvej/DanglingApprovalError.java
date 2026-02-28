package org.approvej;

import org.jspecify.annotations.NullMarked;

/**
 * An {@link AssertionError} thrown when an {@link ApprovalBuilder#approve(Object) approve()} call
 * is not concluded with a terminal method like {@link
 * ApprovalBuilder#by(java.util.function.Function) by()}, {@link ApprovalBuilder#byFile() byFile()},
 * or {@link ApprovalBuilder#byValue(String) byValue()}.
 */
@NullMarked
public class DanglingApprovalError extends AssertionError {

  DanglingApprovalError() {
    super("Dangling approval detected. Call by(), byFile(), or byValue() to conclude the approval");
  }
}
