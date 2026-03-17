package org.approvej.intellij

import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.util.concurrent.atomic.AtomicReference
import org.assertj.core.api.Assertions.assertThat

class ReceivedFileUtilPlatformTest : BasePlatformTestCase() {

  fun testFindApprovedFile_approved_infix() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    val result = ReceivedFileUtil.findApprovedFile(received)

    assertThat(result).isNotNull()
    assertThat(result!!.name).isEqualTo("MyTest.byValue-approved.txt")
  }

  fun testFindApprovedFile_custom_base_name() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    myFixture.addFileToProject("MyTest.byValue.txt", "approved")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    val result = ReceivedFileUtil.findApprovedFile(received)

    assertThat(result).isNotNull()
    assertThat(result!!.name).isEqualTo("MyTest.byValue.txt")
  }

  fun testFindApprovedFile_prefers_approved_infix() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved")
    myFixture.addFileToProject("MyTest.byValue.txt", "base")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    val result = ReceivedFileUtil.findApprovedFile(received)

    assertThat(result).isNotNull()
    assertThat(result!!.name).isEqualTo("MyTest.byValue-approved.txt")
  }

  fun testFindApprovedFile_no_match() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    val result = ReceivedFileUtil.findApprovedFile(received)

    assertThat(result).isNull()
  }

  fun testApproveAction_visible_with_approved_file() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    val presentation = updateAction(ApproveReceivedAction(), received)

    assertThat(presentation.isEnabledAndVisible).isTrue()
  }

  fun testApproveAction_hidden_without_approved_file() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    val presentation = updateAction(ApproveReceivedAction(), received)

    assertThat(presentation.isEnabledAndVisible).isFalse()
  }

  fun testCompareAction_visible_with_approved_file() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    val presentation = updateAction(CompareReceivedWithApprovedAction(), received)

    assertThat(presentation.isEnabledAndVisible).isTrue()
  }

  fun testCompareAction_hidden_without_approved_file() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    val presentation = updateAction(CompareReceivedWithApprovedAction(), received)

    assertThat(presentation.isEnabledAndVisible).isFalse()
  }

  fun testCompareAction_hidden_for_non_received_file() {
    myFixture.addFileToProject("MyTest.java", "class MyTest {}")
    val file = myFixture.findFileInTempDir("MyTest.java")

    val presentation = updateAction(CompareReceivedWithApprovedAction(), file)

    assertThat(presentation.isEnabledAndVisible).isFalse()
  }

  fun testIsActionAvailable_received_with_approved() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    assertThat(ReceivedFileUtil.isActionAvailable(createEvent(received))).isTrue()
  }

  fun testIsActionAvailable_received_without_approved() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    assertThat(ReceivedFileUtil.isActionAvailable(createEvent(received))).isFalse()
  }

  fun testIsActionAvailable_non_received_file() {
    myFixture.addFileToProject("MyTest.java", "class MyTest {}")
    val file = myFixture.findFileInTempDir("MyTest.java")

    assertThat(ReceivedFileUtil.isActionAvailable(createEvent(file))).isFalse()
  }

  fun testWithReceivedAndApproved_invokes_action() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")
    val capturedReceived = AtomicReference<VirtualFile>()
    val capturedApproved = AtomicReference<VirtualFile>()

    ReceivedFileUtil.withReceivedAndApproved(createEvent(received)) { r, a ->
      capturedReceived.set(r)
      capturedApproved.set(a)
    }

    assertThat(capturedReceived.get().name).isEqualTo("MyTest.byValue-received.txt")
    assertThat(capturedApproved.get().name).isEqualTo("MyTest.byValue-approved.txt")
  }

  fun testWithReceivedAndApproved_does_nothing_without_approved() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")
    val capturedReceived = AtomicReference<VirtualFile>()

    ReceivedFileUtil.withReceivedAndApproved(createEvent(received)) { r, _ ->
      capturedReceived.set(r)
    }

    assertThat(capturedReceived.get()).isNull()
  }

  fun testApprove_copies_content_and_deletes_received() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "new content")
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "old content")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")
    val approved = myFixture.findFileInTempDir("MyTest.byValue-approved.txt")

    ReceivedFileUtil.approve(project, received, approved)

    assertThat(received.isValid).isFalse()
    assertThat(String(approved.contentsToByteArray())).isEqualTo("new content")
  }

  fun testReject_deletes_received_without_changing_approved() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "new content")
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "old content")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")
    val approved = myFixture.findFileInTempDir("MyTest.byValue-approved.txt")

    ReceivedFileUtil.reject(project, received)

    assertThat(received.isValid).isFalse()
    assertThat(String(approved.contentsToByteArray())).isEqualTo("old content")
  }

  private fun createEvent(file: VirtualFile): AnActionEvent {
    val dataContext =
      SimpleDataContext.builder()
        .add(CommonDataKeys.VIRTUAL_FILE, file)
        .add(CommonDataKeys.PROJECT, project)
        .build()
    return AnActionEvent.createEvent(dataContext, null, "test", ActionUiKind.NONE, null)
  }

  private fun updateAction(action: AnAction, file: VirtualFile): Presentation {
    val event = createEvent(file)
    ActionUtil.performDumbAwareUpdate(action, event, false)
    return event.presentation
  }
}
