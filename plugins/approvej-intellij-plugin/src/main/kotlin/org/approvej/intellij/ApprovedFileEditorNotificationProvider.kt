package org.approvej.intellij

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import java.util.function.Function
import javax.swing.JComponent

/**
 * Shows an info banner when an approved file is opened, with an action to navigate to the test
 * method that produces it.
 */
class ApprovedFileEditorNotificationProvider : EditorNotificationProvider {

  override fun collectNotificationData(
    project: Project,
    file: VirtualFile,
  ): Function<in FileEditor, out JComponent>? {
    if (!ApprovedFileUtil.isApprovedFile(file)) return null

    val targetMethod = InventoryUtil.findTestMethod(file, project)
    val receivedFile = ApprovedFileUtil.findReceivedFile(file)

    return Function { fileEditor ->
      val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info)
      panel.text = "This is an ApproveJ approved file."
      if (receivedFile != null) {
        panel.createActionLabel("Compare with Received") {
          ReceivedFileUtil.openDiff(project, receivedFile, file)
        }
      }
      if (targetMethod != null) {
        panel.createActionLabel("Navigate to Test") { targetMethod.navigate(true) }
      }
      panel
    }
  }
}
