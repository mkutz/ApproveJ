package org.approvej.intellij

import com.intellij.codeInspection.AbstractBaseUastLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import org.jetbrains.uast.UBlockExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.visitor.AbstractUastVisitor

/**
 * Inspection that detects dangling `approve()` calls that are not concluded with a terminal method
 * (`by()`, `byFile()`, or `byValue()`).
 *
 * Uses UAST to work across both Java and Kotlin.
 */
class DanglingApprovalInspection : AbstractBaseUastLocalInspectionTool() {

  override fun checkMethod(
    method: UMethod,
    manager: InspectionManager,
    isOnTheFly: Boolean,
  ): Array<ProblemDescriptor>? {
    val visitor = DanglingApprovalVisitor()
    method.accept(visitor)
    val problems =
      visitor.danglingApprovals.map { dangling ->
        manager.createProblemDescriptor(
          dangling.approveElement,
          dangling.chainEndElement,
          "Dangling approval: call by(), byFile(), or byValue() to conclude",
          ProblemHighlightType.WARNING,
          isOnTheFly,
          AppendTerminalMethodFix("byFile()"),
          AppendTerminalMethodFix("byValue(\"\")"),
        )
      }
    return if (problems.isEmpty()) null else problems.toTypedArray()
  }

  override fun getStaticDescription(): String =
    """
    Reports <code>approve()</code> calls on <code>ApprovalBuilder</code> that are not \
    concluded with a terminal method (<code>by()</code>, <code>byFile()</code>, or \
    <code>byValue()</code>). A missing terminal call means the approval is never actually \
    checked. When the test method is annotated with <code>@ApprovalTest</code>, this causes \
    a <code>DanglingApprovalError</code> at runtime.\
    """
      .trimIndent()

  private class AppendTerminalMethodFix(private val methodCall: String) : LocalQuickFix {

    override fun getName(): String = "Conclude with .$methodCall"

    override fun getFamilyName(): String = "Conclude dangling approval"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      val chainEnd = descriptor.endElement ?: return
      val document =
        PsiDocumentManager.getInstance(project).getDocument(chainEnd.containingFile) ?: return
      document.insertString(chainEnd.textRange.endOffset, ".$methodCall")
      PsiDocumentManager.getInstance(project).commitDocument(document)
    }
  }

  private data class DanglingApproval(
    val approveElement: PsiElement,
    val chainEndElement: PsiElement,
  )

  private class DanglingApprovalVisitor : AbstractUastVisitor() {

    val danglingApprovals = mutableListOf<DanglingApproval>()

    override fun visitCallExpression(node: UCallExpression): Boolean {
      if (!ApproveCallUtil.isApproveCall(node)) return false

      val chain = ApproveCallUtil.findTerminalCall(node)
      val current = chain.chainEnd

      if (chain.lastMethodName in TERMINAL_METHODS) return false

      val chainParent = current.uastParent
      if (chainParent !is UBlockExpression && chainParent !is ULambdaExpression) return false

      val approveSourcePsi = node.sourcePsi ?: return false
      val chainEndSourcePsi = current.sourcePsi ?: approveSourcePsi

      danglingApprovals.add(DanglingApproval(approveSourcePsi, chainEndSourcePsi))
      return false
    }
  }

  companion object {
    private val TERMINAL_METHODS = setOf("by", "byFile", "byValue")
  }
}
