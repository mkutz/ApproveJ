package org.approvej.intellij

import com.intellij.execution.testframework.AbstractTestProxy
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/** Action that approves the received file for a single test selected in the test runner tree. */
class ApproveTestAction : AnAction() {

  override fun update(event: AnActionEvent) {
    event.presentation.isEnabledAndVisible = TestProxyUtil.isActionAvailable(event)
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun actionPerformed(event: AnActionEvent) {
    val project = event.project ?: return
    val proxy = event.getData(AbstractTestProxy.DATA_KEY) ?: return
    for ((received, approved) in TestProxyUtil.findReceivedFiles(proxy, project)) {
      ReceivedFileUtil.approve(project, received, approved)
    }
  }
}
