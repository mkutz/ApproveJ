package org.approvej.intellij

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import java.util.function.Function
import javax.swing.JComponent

/** Shows an info banner when a `-received` file is opened, with actions to compare or approve. */
class ReceivedFileEditorNotificationProvider : EditorNotificationProvider {

  override fun collectNotificationData(
    project: Project,
    file: VirtualFile,
  ): Function<in FileEditor, out JComponent>? {
    if (!ReceivedFileUtil.isReceivedFile(file)) return null
    val approvedFile = ReceivedFileUtil.findApprovedFile(file)
    val targetMethod =
      if (approvedFile != null) InventoryUtil.findTestMethod(approvedFile, project) else null
    return Function { fileEditor ->
      val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info)
      if (approvedFile != null) {
        panel.text = "This is an ApproveJ received file that has not been approved yet."
        panel.createActionLabel("Compare with Approved") {
          ReceivedFileUtil.openDiff(project, file, approvedFile)
        }
        panel.createActionLabel("Approve") { ReceivedFileUtil.approve(project, file, approvedFile) }
        panel.createActionLabel("Reject") { ReceivedFileUtil.reject(project, file) }
      } else {
        panel.text =
          "This is an ApproveJ received file. No matching approved file was found nearby."
      }
      if (targetMethod != null) {
        panel.createActionLabel("Navigate to Test") { targetMethod.navigate(true) }
      }
      panel
    }
  }
}
