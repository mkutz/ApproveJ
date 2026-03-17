package org.approvej.intellij

import com.intellij.psi.PsiElement
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.getUParentForIdentifier

/** Shared utilities for recognising and analysing `approve()` call chains. */
internal object ApproveCallUtil {

  const val APPROVAL_BUILDER_CLASS = "org.approvej.ApprovalBuilder"

  /**
   * Returns the UAST call expression if the given PSI element is the identifier of an `approve()`
   * call on `ApprovalBuilder`, or `null` otherwise.
   */
  fun asApproveCall(element: PsiElement): UCallExpression? {
    if (element.firstChild != null) return null
    val uElement = getUParentForIdentifier(element)
    return (uElement as? UCallExpression)?.takeIf { isApproveCall(it) }
  }

  /**
   * Returns `true` if the given `approve()` call is on `ApprovalBuilder`.
   *
   * Unlike [asApproveCall], this takes a UAST node directly (no leaf-element check).
   */
  fun isApproveCall(node: UCallExpression): Boolean {
    if (node.methodName != "approve") return false
    val method = node.resolve() ?: return false
    val containingClass = method.containingClass ?: return false
    return containingClass.qualifiedName == APPROVAL_BUILDER_CLASS
  }

  /**
   * Walks the UAST parent chain from an `approve()` call and returns the name of the last method in
   * the chain (e.g. `"byFile"`, `"printedAs"`, or `"approve"` if there is no chained call).
   */
  fun findTerminalCall(approveCall: UCallExpression): ChainWalkResult {
    var current: org.jetbrains.uast.UElement = approveCall
    var lastMethodName = "approve"
    while (true) {
      val parent = current.uastParent
      if (parent is UQualifiedReferenceExpression) {
        val selector = parent.selector
        if (selector is UCallExpression && selector.methodName != null) {
          lastMethodName = selector.methodName!!
          current = parent
          continue
        }
      }
      break
    }
    return ChainWalkResult(lastMethodName, current)
  }

  /**
   * Walks the UAST parent chain from an `approve()` call and returns the string literal argument of
   * a `.named()` call in the chain, or `null` if no such call exists.
   */
  fun findNamedArgument(approveCall: UCallExpression): String? {
    var current: org.jetbrains.uast.UElement = approveCall
    while (true) {
      val parent = current.uastParent
      if (parent is UQualifiedReferenceExpression) {
        val selector = parent.selector
        if (selector is UCallExpression && selector.methodName == "named") {
          val args: List<UExpression> = selector.valueArguments
          if (args.size == 1) {
            val evaluated = args.first().evaluate()
            if (evaluated is String) return evaluated
          }
        }
        current = parent
        continue
      }
      break
    }
    return null
  }

  /**
   * Returns `true` if the UAST parent chain from an `approve()` call contains a `.named()` call,
   * regardless of whether the argument is a constant.
   */
  fun hasNamedCall(approveCall: UCallExpression): Boolean {
    var current: org.jetbrains.uast.UElement = approveCall
    while (true) {
      val parent = current.uastParent
      if (parent is UQualifiedReferenceExpression) {
        val selector = parent.selector
        if (selector is UCallExpression && selector.methodName == "named") return true
        current = parent
        continue
      }
      break
    }
    return false
  }

  data class ChainWalkResult(val lastMethodName: String, val chainEnd: org.jetbrains.uast.UElement)
}
