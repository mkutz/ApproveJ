package org.approvej.intellij

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Action that approves a received file by copying its content to the approved file and deleting the
 * received file.
 */
class ApproveReceivedAction : AnAction() {

  override fun update(event: AnActionEvent) {
    event.presentation.isEnabledAndVisible = ReceivedFileUtil.isActionAvailable(event)
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun actionPerformed(event: AnActionEvent) {
    val project = event.project ?: return
    ReceivedFileUtil.withReceivedAndApproved(event) { received, approved ->
      ReceivedFileUtil.approve(project, received, approved)
    }
  }
}
