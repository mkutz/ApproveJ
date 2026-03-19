package org.approvej.intellij

import com.intellij.diff.DiffContext
import com.intellij.diff.DiffExtension
import com.intellij.diff.FrameDiffTool
import com.intellij.diff.requests.DiffRequest
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiMethod
import com.intellij.ui.EditorNotificationPanel
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Adds a notification panel with "Navigate to Test", "Approve", and "Reject" actions to the diff
 * viewer when comparing an ApproveJ received file with its approved counterpart.
 */
class ApproveJDiffExtension : DiffExtension() {

  override fun onViewerCreated(
    viewer: FrameDiffTool.DiffViewer,
    context: DiffContext,
    request: DiffRequest,
  ) {
    if (viewer is ImageDiffTool.ImageDiffViewer) return
    val receivedFile = request.getUserData(ReceivedFileUtil.RECEIVED_FILE_KEY) ?: return
    val approvedFile = request.getUserData(ReceivedFileUtil.APPROVED_FILE_KEY) ?: return
    val project = context.project ?: return

    val testMethod = InventoryUtil.findTestMethod(approvedFile, project)
    val panel =
      createNotificationPanel(project, receivedFile, approvedFile, testMethod) {
        FileEditorManager.getInstance(project).openFile(approvedFile, true)
      }
    addToViewerNotifications(viewer, panel)
  }

  companion object {
    internal fun createNotificationPanel(
      project: Project,
      receivedFile: VirtualFile,
      approvedFile: VirtualFile,
      testMethod: PsiMethod?,
      afterAction: (() -> Unit)? = null,
    ): JComponent {
      val panel = EditorNotificationPanel(EditorNotificationPanel.Status.Info)
      panel.text = "ApproveJ: Received vs. Approved"
      if (testMethod != null) {
        panel.createActionLabel("Navigate to Test") { testMethod.navigate(true) }
      }
      panel.createActionLabel("Approve") {
        ReceivedFileUtil.approve(project, receivedFile, approvedFile)
        afterAction?.invoke()
      }
      panel.createActionLabel("Reject") {
        ReceivedFileUtil.reject(project, receivedFile)
        afterAction?.invoke()
      }
      return panel
    }

    private fun addToViewerNotifications(viewer: FrameDiffTool.DiffViewer, panel: JComponent) {
      val component = viewer.component
      val layout = component.layout
      if (layout is BorderLayout) {
        val north = layout.getLayoutComponent(BorderLayout.NORTH)
        if (north is JPanel) {
          north.add(panel, 0)
        }
      }
    }
  }
}
