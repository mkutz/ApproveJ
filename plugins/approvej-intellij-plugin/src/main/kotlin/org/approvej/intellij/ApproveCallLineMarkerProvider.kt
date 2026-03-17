package org.approvej.intellij

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import javax.swing.Icon
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.getParentOfType

/**
 * Provides a gutter icon on `approve()...byFile()` chains that navigates to the approved file. When
 * a received file also exists, clicking the icon opens the diff viewer comparing the received file
 * with the approved file.
 */
class ApproveCallLineMarkerProvider : LineMarkerProviderDescriptor() {

  override fun getName(): String = "ApproveJ approved file"

  override fun getIcon(): Icon = ApproveJIcons.APPROVED

  override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null

  override fun collectSlowLineMarkers(
    elements: List<PsiElement>,
    result: MutableCollection<in LineMarkerInfo<*>>,
  ) {
    for (element in elements) {
      collectMarker(element, result)
    }
  }

  private fun collectMarker(element: PsiElement, result: MutableCollection<in LineMarkerInfo<*>>) {
    val callExpression = ApproveCallUtil.asApproveCall(element) ?: return

    if (ApproveCallUtil.findTerminalCall(callExpression).lastMethodName != "byFile") return

    val uMethod = callExpression.getParentOfType<UMethod>() ?: return
    val psiMethod = uMethod.javaPsi
    val psiClass = psiMethod.containingClass ?: return
    val className = psiClass.qualifiedName ?: return

    val methodName = psiMethod.name
    val project = element.project

    val affix = ApproveCallUtil.findNamedArgument(callExpression)
    val suffix = if (affix != null) "$affix-approved" else "$methodName-approved"

    val approvedFiles =
      InventoryUtil.findApprovedFiles(className, methodName, project).filter { approvedFile ->
        val name = approvedFile.nameWithoutExtension
        val prefixLen = name.length - suffix.length
        name.endsWith(suffix) && (prefixLen == 0 || name[prefixLen - 1] == '-')
      }
    if (approvedFiles.isEmpty()) return

    val psiManager = PsiManager.getInstance(project)
    val targets = approvedFiles.mapNotNull { psiManager.findFile(it) }
    if (targets.isEmpty()) return

    data class ReceivedApprovedPair(val received: VirtualFile, val approved: VirtualFile)

    val receivedApprovedPair =
      approvedFiles.firstNotNullOfOrNull { approved ->
        ApprovedFileUtil.findReceivedFile(approved)?.let { ReceivedApprovedPair(it, approved) }
      }

    if (receivedApprovedPair == null) {
      val builder =
        NavigationGutterIconBuilder.create(ApproveJIcons.APPROVED)
          .setTargets(targets)
          .setTooltipText(
            if (targets.size == 1) "Navigate to ${approvedFiles.first().name}"
            else "Navigate to approved file"
          )
      result.add(builder.createLineMarkerInfo(element))
    } else {
      val (receivedFile, approvedFile) = receivedApprovedPair
      val markerInfo =
        LineMarkerInfo(
          element,
          element.textRange,
          ApproveJIcons.APPROVAL_PENDING,
          { "Compare received with approved" },
          { _, elt -> ReceivedFileUtil.openDiff(elt.project, receivedFile, approvedFile) },
          GutterIconRenderer.Alignment.LEFT,
        ) {
          "Compare received with approved"
        }
      result.add(markerInfo)
    }
  }
}
