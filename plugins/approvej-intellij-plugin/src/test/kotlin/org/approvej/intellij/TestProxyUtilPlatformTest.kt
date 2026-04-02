package org.approvej.intellij

import com.intellij.execution.testframework.AbstractTestProxy
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.assertj.core.api.Assertions.assertThat

class TestProxyUtilPlatformTest : BasePlatformTestCase() {

  fun testApproveTestAction_hidden_without_proxy() {
    val presentation = updateAction(ApproveTestAction(), null)

    assertThat(presentation.isEnabledAndVisible).isFalse()
  }

  fun testApproveTestAction_hidden_for_suite_node() {
    val suite = com.intellij.execution.testframework.sm.runner.SMTestProxy("MySuite", true, null)

    val presentation = updateAction(ApproveTestAction(), suite)

    assertThat(presentation.isEnabledAndVisible).isFalse()
  }

  fun testApproveTestAction_hidden_for_leaf_without_received_files() {
    val leaf = com.intellij.execution.testframework.sm.runner.SMTestProxy("myTest", false, null)

    val presentation = updateAction(ApproveTestAction(), leaf)

    assertThat(presentation.isEnabledAndVisible).isFalse()
  }

  fun testCompareTestAction_hidden_without_proxy() {
    val presentation = updateAction(CompareTestAction(), null)

    assertThat(presentation.isEnabledAndVisible).isFalse()
  }

  fun testCompareTestAction_hidden_for_suite_node() {
    val suite = com.intellij.execution.testframework.sm.runner.SMTestProxy("MySuite", true, null)

    val presentation = updateAction(CompareTestAction(), suite)

    assertThat(presentation.isEnabledAndVisible).isFalse()
  }

  fun testCompareTestAction_hidden_for_leaf_without_received_files() {
    val leaf = com.intellij.execution.testframework.sm.runner.SMTestProxy("myTest", false, null)

    val presentation = updateAction(CompareTestAction(), leaf)

    assertThat(presentation.isEnabledAndVisible).isFalse()
  }

  private fun updateAction(action: AnAction, proxy: AbstractTestProxy?): Presentation {
    val builder = SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project)
    if (proxy != null) {
      builder.add(AbstractTestProxy.DATA_KEY, proxy)
    }
    val event = AnActionEvent.createEvent(builder.build(), null, "test", ActionUiKind.NONE, null)
    ActionUtil.performDumbAwareUpdate(action, event, false)
    return event.presentation
  }
}
