package org.approvej.intellij

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/** Action that opens the diff viewer comparing a received file with its approved counterpart. */
class CompareReceivedWithApprovedAction : AnAction() {

  override fun update(event: AnActionEvent) {
    event.presentation.isEnabledAndVisible = ReceivedFileUtil.isActionAvailable(event)
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun actionPerformed(event: AnActionEvent) {
    val project = event.project ?: return
    ReceivedFileUtil.withReceivedAndApproved(event) { received, approved ->
      ReceivedFileUtil.openDiff(project, received, approved)
    }
  }
}
