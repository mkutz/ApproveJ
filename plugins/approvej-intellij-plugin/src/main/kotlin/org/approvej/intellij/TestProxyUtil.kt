package org.approvej.intellij

import com.intellij.execution.testframework.AbstractTestProxy
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope

/** Resolves received/approved file pairs from test runner tree nodes. */
internal object TestProxyUtil {

  /**
   * Returns `true` if the event contains a leaf test proxy with at least one received file waiting
   * for approval.
   */
  fun isActionAvailable(event: AnActionEvent): Boolean {
    val project = event.project ?: return false
    val proxy = event.getData(AbstractTestProxy.DATA_KEY) ?: return false
    return proxy.isLeaf && findReceivedFiles(proxy, project).isNotEmpty()
  }

  /** Resolves received+approved file pairs for a single leaf test node. */
  fun findReceivedFiles(
    proxy: AbstractTestProxy,
    project: Project,
  ): List<Pair<VirtualFile, VirtualFile>> {
    val location =
      proxy.getLocation(project, GlobalSearchScope.projectScope(project)) ?: return emptyList()
    val psiMethod = location.psiElement as? PsiMethod ?: return emptyList()
    val className = psiMethod.containingClass?.qualifiedName ?: return emptyList()
    val methodName = psiMethod.name
    return InventoryUtil.findApprovedFiles(className, methodName, project).mapNotNull { approved ->
      val received = ApprovedFileUtil.findReceivedFile(approved) ?: return@mapNotNull null
      received to approved
    }
  }
}
