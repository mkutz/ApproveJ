package org.approvej.intellij

import com.intellij.execution.testframework.AbstractTestProxy
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Action that opens the diff viewer comparing the received file with the approved file for a single
 * test selected in the test runner tree.
 */
class CompareTestAction : AnAction() {

  override fun update(event: AnActionEvent) {
    val project = event.project
    val proxy = event.getData(AbstractTestProxy.DATA_KEY)
    event.presentation.isEnabledAndVisible =
      project != null &&
        proxy != null &&
        proxy.isLeaf &&
        TestProxyUtil.findReceivedFiles(proxy, project).isNotEmpty()
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun actionPerformed(event: AnActionEvent) {
    val project = event.project ?: return
    val proxy = event.getData(AbstractTestProxy.DATA_KEY) ?: return
    for ((received, approved) in TestProxyUtil.findReceivedFiles(proxy, project)) {
      ReceivedFileUtil.openDiff(project, received, approved)
    }
  }
}
