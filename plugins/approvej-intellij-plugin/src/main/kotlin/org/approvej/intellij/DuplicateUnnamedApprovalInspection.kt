package org.approvej.intellij

import com.intellij.codeInspection.AbstractBaseUastLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.visitor.AbstractUastVisitor

/**
 * Inspection that detects multiple `approve().byFile()` calls within the same test method that
 * resolve to the same approved file.
 *
 * Without `.named()`, each no-arg `byFile()` uses the test method name, so the second call silently
 * overwrites the first.
 */
class DuplicateUnnamedApprovalInspection : AbstractBaseUastLocalInspectionTool() {

  override fun checkMethod(
    method: UMethod,
    manager: InspectionManager,
    isOnTheFly: Boolean,
  ): Array<ProblemDescriptor>? {
    val collector = NoArgByFileCallCollector()
    method.accept(collector)

    val countsByName = collector.calls.groupingBy { it.effectiveName ?: "" }.eachCount()

    val problems =
      collector.calls
        .filter { countsByName.getOrDefault(it.effectiveName ?: "", 0) > 1 }
        .map { call ->
          val key = call.effectiveName ?: ""
          val unnamed = key.isEmpty()
          val message = if (unnamed) UNNAMED_WARNING else NAMED_WARNING_TEMPLATE.format(key)
          val fixes =
            if (unnamed) arrayOf<LocalQuickFix>(AddNamedCallFix()) else LocalQuickFix.EMPTY_ARRAY
          manager.createProblemDescriptor(
            call.approveElement,
            call.chainEndElement,
            message,
            ProblemHighlightType.WARNING,
            isOnTheFly,
            *fixes,
          )
        }

    return if (problems.isEmpty()) null else problems.toTypedArray()
  }

  override fun getStaticDescription(): String =
    "Reports multiple <code>approve().byFile()</code> calls within the same test method " +
      "that resolve to the same approved file. Without <code>.named()</code>, each no-arg " +
      "<code>byFile()</code> uses the test method name, so the second call silently overwrites " +
      "the first. Use <code>.named()</code> to give each approval a distinct name."

  private data class NoArgByFileCall(
    val approveElement: PsiElement,
    val chainEndElement: PsiElement,
    val effectiveName: String?,
  )

  private class NoArgByFileCallCollector : AbstractUastVisitor() {

    val calls = mutableListOf<NoArgByFileCall>()

    override fun visitCallExpression(node: UCallExpression): Boolean {
      if (!ApproveCallUtil.isApproveCall(node)) return false

      val chain = ApproveCallUtil.findTerminalCall(node)
      if (chain.lastMethodName != "byFile") return false

      val chainEnd = chain.chainEnd
      if (chainEnd is UQualifiedReferenceExpression) {
        val selector = chainEnd.selector
        if (selector is UCallExpression && selector.valueArguments.isNotEmpty()) return false
      }

      val approveSourcePsi = node.sourcePsi ?: return false
      val chainEndSourcePsi = chain.chainEnd.sourcePsi ?: return false

      val namedArg = ApproveCallUtil.findNamedArgument(node)
      if (namedArg == null && ApproveCallUtil.hasNamedCall(node)) return false
      calls.add(NoArgByFileCall(approveSourcePsi, chainEndSourcePsi, namedArg))
      return false
    }
  }

  private class AddNamedCallFix : LocalQuickFix {

    override fun getName(): String = "Add .named(\"TODO\")"

    override fun getFamilyName(): String = "Add .named(\"TODO\")"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      val element = descriptor.endElement ?: return
      val document =
        PsiDocumentManager.getInstance(project).getDocument(element.containingFile) ?: return

      val offset = findByFileInsertOffset(element)
      if (offset < 0) return

      val insertion = ".named(\"TODO\")"
      document.insertString(offset, insertion)
      PsiDocumentManager.getInstance(project).commitDocument(document)

      val todoStart = offset + ".named(\"".length
      val todoEnd = todoStart + "TODO".length
      val editor = FileEditorManager.getInstance(project).selectedTextEditor
      if (editor != null && editor.document == document) {
        editor.caretModel.moveToOffset(todoStart)
        editor.selectionModel.setSelection(todoStart, todoEnd)
      }
    }
  }

  companion object {
    private const val UNNAMED_WARNING =
      "Duplicate unnamed approval: each subsequent byFile() overwrites the previous approved file." +
        " Use .named() to distinguish."

    private const val NAMED_WARNING_TEMPLATE =
      "Duplicate approval name '%s': each subsequent byFile() overwrites the previous approved" +
        " file. Use distinct names."

    private fun findByFileInsertOffset(chainEndElement: PsiElement): Int {
      val text = chainEndElement.text
      val dotByFile = text.lastIndexOf(".byFile(")
      if (dotByFile == -1) return -1
      return chainEndElement.textRange.startOffset + dotByFile
    }
  }
}
