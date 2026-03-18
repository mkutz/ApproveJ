package org.approvej.intellij

import com.intellij.psi.PsiElement
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.getUParentForIdentifier

/** Shared utilities for recognising and analysing `approve()` call chains. */
internal object ApproveCallUtil {

  const val APPROVAL_BUILDER_CLASS = "org.approvej.ApprovalBuilder"
  const val IMAGE_APPROVAL_BUILDER_CLASS = "org.approvej.image.ImageApprovalBuilder"

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
    val name = node.methodName
    if (name != "approve" && name != "approveImage") return false
    val method = node.resolve() ?: return false
    val containingClass = method.containingClass ?: return false
    val qname = containingClass.qualifiedName
    return qname == APPROVAL_BUILDER_CLASS || qname == IMAGE_APPROVAL_BUILDER_CLASS
  }

  /**
   * Walks the UAST parent chain from an `approve()` call and returns the name of the last method in
   * the chain (e.g. `"byFile"`, `"printedAs"`, or `"approve"` if there is no chained call).
   */
  fun findTerminalCall(approveCall: UCallExpression): ChainWalkResult {
    var lastMethodName = approveCall.methodName ?: "approve"
    val chainEnd =
      walkChain(approveCall) { selector ->
        val name = selector.methodName
        if (name != null) {
          lastMethodName = name
          true
        } else {
          false
        }
      }
    return ChainWalkResult(lastMethodName, chainEnd)
  }

  /**
   * Walks the UAST parent chain from an `approve()` call and returns the string literal argument of
   * a `.named()` call in the chain, or `null` if no such call exists.
   */
  fun findNamedArgument(approveCall: UCallExpression): String? {
    walkChain(approveCall) { selector ->
      if (selector.methodName == "named") {
        val args: List<UExpression> = selector.valueArguments
        if (args.size == 1) {
          val evaluated = args.first().evaluate()
          if (evaluated is String) return evaluated
        }
      }
      true
    }
    return null
  }

  /**
   * Returns `true` if the UAST parent chain from an `approve()` call contains a `.named()` call,
   * regardless of whether the argument is a constant.
   */
  fun hasNamedCall(approveCall: UCallExpression): Boolean {
    walkChain(approveCall) { selector ->
      if (selector.methodName == "named") return true
      true
    }
    return false
  }

  /**
   * Walks the qualified-reference parent chain from [start], calling [visitSelector] for each
   * chained [UCallExpression]. The visitor returns `true` to continue walking or `false` to stop.
   * Returns the outermost [UElement] reached.
   */
  private inline fun walkChain(
    start: UCallExpression,
    visitSelector: (UCallExpression) -> Boolean,
  ): UElement {
    var current: UElement = start
    while (true) {
      val parent = current.uastParent
      if (parent is UQualifiedReferenceExpression) {
        val selector = parent.selector
        if (selector is UCallExpression) {
          if (!visitSelector(selector)) break
          current = parent
          continue
        }
      }
      break
    }
    return current
  }

  data class ChainWalkResult(val lastMethodName: String, val chainEnd: UElement)
}
