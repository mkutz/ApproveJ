package org.approvej.intellij;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import java.util.List;
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

  /**
   * Walks the UAST parent chain from an {@code approve()} call and returns the string literal
   * argument of a {@code .named()} call in the chain, or {@code null} if no such call exists.
   */
  static @Nullable String findNamedArgument(@NotNull UCallExpression approveCall) {
    UElement current = approveCall;
    while (true) {
      UElement parent = current.getUastParent();
      if (parent instanceof UQualifiedReferenceExpression qualRef) {
        UExpression selector = qualRef.getSelector();
        if (selector instanceof UCallExpression selectorCall
            && "named".equals(selectorCall.getMethodName())) {
          List<UExpression> args = selectorCall.getValueArguments();
          if (args.size() == 1) {
            Object evaluated = args.getFirst().evaluate();
            if (evaluated instanceof String s) {
              return s;
            }
          }
        }
        current = qualRef;
        continue;
      }
      break;
    }
    return null;
  }

  record ChainWalkResult(String lastMethodName, UElement chainEnd) {}
}
