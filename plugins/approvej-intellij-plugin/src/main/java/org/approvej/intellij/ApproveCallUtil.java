package org.approvej.intellij;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.UQualifiedReferenceExpression;
import org.jetbrains.uast.UastUtils;

/** Shared utilities for recognising and analysing {@code approve()} call chains. */
final class ApproveCallUtil {

  static final String APPROVAL_BUILDER_CLASS = "org.approvej.ApprovalBuilder";

  private ApproveCallUtil() {}

  /**
   * Returns the UAST call expression if the given PSI element is the identifier of an {@code
   * approve()} call on {@code ApprovalBuilder}, or {@code null} otherwise.
   */
  static @Nullable UCallExpression asApproveCall(@NotNull PsiElement element) {
    if (element.getFirstChild() != null) return null;

    UElement uElement = UastUtils.getUParentForIdentifier(element);
    if (!(uElement instanceof UCallExpression callExpression)) return null;
    return isApproveCall(callExpression) ? callExpression : null;
  }

  /**
   * Returns {@code true} if the given {@code approve()} call is on {@code ApprovalBuilder}.
   *
   * <p>Unlike {@link #asApproveCall}, this takes a UAST node directly (no leaf-element check).
   */
  static boolean isApproveCall(@NotNull UCallExpression node) {
    if (!"approve".equals(node.getMethodName())) return false;

    PsiMethod method = node.resolve();
    if (method == null) return false;
    PsiClass containingClass = method.getContainingClass();
    return containingClass != null
        && APPROVAL_BUILDER_CLASS.equals(containingClass.getQualifiedName());
  }

  /**
   * Walks the UAST parent chain from an {@code approve()} call and returns the name of the last
   * method in the chain (e.g. {@code "byFile"}, {@code "printedAs"}, or {@code "approve"} if there
   * is no chained call).
   */
  static ChainWalkResult findTerminalCall(@NotNull UCallExpression approveCall) {
    UElement current = approveCall;
    String lastMethodName = "approve";
    while (true) {
      UElement parent = current.getUastParent();
      if (parent instanceof UQualifiedReferenceExpression qualRef) {
        UExpression selector = qualRef.getSelector();
        if (selector instanceof UCallExpression selectorCall
            && selectorCall.getMethodName() != null) {
          lastMethodName = selectorCall.getMethodName();
          current = qualRef;
          continue;
        }
      }
      break;
    }
    return new ChainWalkResult(lastMethodName, current);
  }

  record ChainWalkResult(String lastMethodName, UElement chainEnd) {}
}
