package org.approvej.intellij

import com.intellij.diff.DiffDialogHints
import com.intellij.diff.DiffManager
import com.intellij.diff.DiffRequestPanel
import com.intellij.diff.chains.DiffRequestChain
import com.intellij.diff.merge.MergeRequest
import com.intellij.diff.requests.DiffRequest
import com.intellij.execution.testframework.AbstractTestProxy
import com.intellij.execution.testframework.sm.runner.SMTestProxy
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import com.intellij.testFramework.replaceService
import org.assertj.core.api.Assertions.assertThat

class TestProxyUtilPlatformTest : LightJavaCodeInsightFixtureTestCase() {

  fun testApproveTestAction_hidden_without_proxy() {
    val presentation = updateAction(ApproveTestAction(), null)

    assertThat(presentation.isEnabledAndVisible).isFalse()
  }

  fun testApproveTestAction_hidden_for_suite_node() {
    val suite = SMTestProxy("MySuite", true, null)

    val presentation = updateAction(ApproveTestAction(), suite)

    assertThat(presentation.isEnabledAndVisible).isFalse()
  }

  fun testApproveTestAction_hidden_for_leaf_without_received_files() {
    val leaf = SMTestProxy("myTest", false, null)

    val presentation = updateAction(ApproveTestAction(), leaf)

    assertThat(presentation.isEnabledAndVisible).isFalse()
  }

  fun testApproveTestAction_visible_for_leaf_with_received_files() {
    addTestClassWithMethod("com.example", "MyTest", "byValue")
    addInventory("src/com/example/MyTest-byValue-approved.txt" to "com.example.MyTest#byValue")
    myFixture.addFileToProject("src/com/example/MyTest-byValue-approved.txt", "approved")
    myFixture.addFileToProject("src/com/example/MyTest-byValue-received.txt", "received")
    val proxy = createLeafProxy("com.example.MyTest", "byValue")

    val presentation = updateAction(ApproveTestAction(), proxy)

    assertThat(presentation.isEnabledAndVisible).isTrue()
  }

  fun testCompareTestAction_hidden_without_proxy() {
    val presentation = updateAction(CompareTestAction(), null)

    assertThat(presentation.isEnabledAndVisible).isFalse()
  }

  fun testCompareTestAction_hidden_for_suite_node() {
    val suite = SMTestProxy("MySuite", true, null)

    val presentation = updateAction(CompareTestAction(), suite)

    assertThat(presentation.isEnabledAndVisible).isFalse()
  }

  fun testCompareTestAction_hidden_for_leaf_without_received_files() {
    val leaf = SMTestProxy("myTest", false, null)

    val presentation = updateAction(CompareTestAction(), leaf)

    assertThat(presentation.isEnabledAndVisible).isFalse()
  }

  fun testCompareTestAction_visible_for_leaf_with_received_files() {
    addTestClassWithMethod("com.example", "MyTest", "byValue")
    addInventory("src/com/example/MyTest-byValue-approved.txt" to "com.example.MyTest#byValue")
    myFixture.addFileToProject("src/com/example/MyTest-byValue-approved.txt", "approved")
    myFixture.addFileToProject("src/com/example/MyTest-byValue-received.txt", "received")
    val proxy = createLeafProxy("com.example.MyTest", "byValue")

    val presentation = updateAction(CompareTestAction(), proxy)

    assertThat(presentation.isEnabledAndVisible).isTrue()
  }

  fun testApproveTestAction_approves_received_file() {
    addTestClassWithMethod("com.example", "MyTest", "byValue")
    addInventory("src/com/example/MyTest-byValue-approved.txt" to "com.example.MyTest#byValue")
    myFixture.addFileToProject("src/com/example/MyTest-byValue-approved.txt", "old content")
    myFixture.addFileToProject("src/com/example/MyTest-byValue-received.txt", "new content")
    val proxy = createLeafProxy("com.example.MyTest", "byValue")

    ApproveTestAction().actionPerformed(createActionEvent(proxy))

    val approved = myFixture.findFileInTempDir("src/com/example/MyTest-byValue-approved.txt")
    assertThat(String(approved.contentsToByteArray())).isEqualTo("new content")
    assertThat(myFixture.findFileInTempDir("src/com/example/MyTest-byValue-received.txt")).isNull()
  }

  fun testCompareTestAction_opens_diff() {
    addTestClassWithMethod("com.example", "MyTest", "byValue")
    addInventory("src/com/example/MyTest-byValue-approved.txt" to "com.example.MyTest#byValue")
    myFixture.addFileToProject("src/com/example/MyTest-byValue-approved.txt", "approved")
    myFixture.addFileToProject("src/com/example/MyTest-byValue-received.txt", "received")
    val proxy = createLeafProxy("com.example.MyTest", "byValue")
    val diffManager = RecordingDiffManager()
    com.intellij.openapi.application.ApplicationManager.getApplication()
      .replaceService(DiffManager::class.java, diffManager, testRootDisposable)

    CompareTestAction().actionPerformed(createActionEvent(proxy))

    assertThat(diffManager.requests).hasSize(1)
    assertThat(diffManager.requests[0].title).isEqualTo("ApproveJ: MyTest-byValue-received.txt")
  }

  fun testFindReceivedFiles_returns_received_and_approved_pair() {
    addTestClassWithMethod("com.example", "MyTest", "byValue")
    addInventory("src/com/example/MyTest-byValue-approved.txt" to "com.example.MyTest#byValue")
    myFixture.addFileToProject("src/com/example/MyTest-byValue-approved.txt", "approved")
    myFixture.addFileToProject("src/com/example/MyTest-byValue-received.txt", "received")
    val proxy = createLeafProxy("com.example.MyTest", "byValue")

    val result = TestProxyUtil.findReceivedFiles(proxy, project)

    assertThat(result).hasSize(1)
    assertThat(result[0].first.name).isEqualTo("MyTest-byValue-received.txt")
    assertThat(result[0].second.name).isEqualTo("MyTest-byValue-approved.txt")
  }

  fun testFindReceivedFiles_empty_when_no_received_file() {
    addTestClassWithMethod("com.example", "MyTest", "byValue")
    addInventory("src/com/example/MyTest-byValue-approved.txt" to "com.example.MyTest#byValue")
    myFixture.addFileToProject("src/com/example/MyTest-byValue-approved.txt", "approved")
    val proxy = createLeafProxy("com.example.MyTest", "byValue")

    val result = TestProxyUtil.findReceivedFiles(proxy, project)

    assertThat(result).isEmpty()
  }

  private fun createLeafProxy(className: String, methodName: String): SMTestProxy {
    val proxy = SMTestProxy(methodName, false, "java:test://$className/$methodName")
    proxy.setLocator(com.intellij.execution.testframework.JavaTestLocator.INSTANCE)
    return proxy
  }

  private fun addTestClassWithMethod(packageName: String, className: String, methodName: String) {
    myFixture.addClass(
      """
      package $packageName;
      public class $className {
          void $methodName() {}
      }
      """
        .trimIndent()
    )
  }

  private fun addInventory(vararg entries: Pair<String, String>) {
    val content = buildString {
      appendLine("# ApproveJ Approved File Inventory")
      entries.forEach { (path, testRef) -> appendLine("${path.replace(" ", "\\ ")} = $testRef") }
    }
    myFixture.addFileToProject(".approvej/inventory.properties", content)
  }

  private fun createActionEvent(proxy: AbstractTestProxy?): AnActionEvent {
    val builder = SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project)
    if (proxy != null) {
      builder.add(AbstractTestProxy.DATA_KEY, proxy)
    }
    return AnActionEvent.createEvent(builder.build(), null, "test", ActionUiKind.NONE, null)
  }

  private fun updateAction(action: AnAction, proxy: AbstractTestProxy?): Presentation {
    val event = createActionEvent(proxy)
    ActionUtil.performDumbAwareUpdate(action, event, false)
    return event.presentation
  }

  private class RecordingDiffManager : DiffManager() {
    val requests = mutableListOf<DiffRequest>()

    override fun showDiff(project: Project?, request: DiffRequest) {
      requests.add(request)
    }

    override fun showDiff(project: Project?, request: DiffRequest, hints: DiffDialogHints) {
      requests.add(request)
    }

    override fun showDiff(project: Project?, chain: DiffRequestChain, hints: DiffDialogHints) {}

    override fun createRequestPanel(
      project: Project?,
      disposable: Disposable,
      window: java.awt.Window?,
    ): DiffRequestPanel {
      throw UnsupportedOperationException()
    }

    override fun showMerge(project: Project?, request: MergeRequest) {}
  }
}
