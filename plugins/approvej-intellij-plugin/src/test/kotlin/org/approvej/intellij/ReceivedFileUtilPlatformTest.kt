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

class ReceivedFileUtilPlatformTest : BasePlatformTestCase() {

  fun testFindApprovedFile_approved_infix() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    val result = ReceivedFileUtil.findApprovedFile(received)

    assertNotNull(result)
    assertEquals("MyTest.byValue-approved.txt", result!!.name)
  }

  fun testFindApprovedFile_custom_base_name() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    myFixture.addFileToProject("MyTest.byValue.txt", "approved")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    val result = ReceivedFileUtil.findApprovedFile(received)

    assertNotNull(result)
    assertEquals("MyTest.byValue.txt", result!!.name)
  }

  fun testFindApprovedFile_prefers_approved_infix() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved")
    myFixture.addFileToProject("MyTest.byValue.txt", "base")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    val result = ReceivedFileUtil.findApprovedFile(received)

    assertNotNull(result)
    assertEquals("MyTest.byValue-approved.txt", result!!.name)
  }

  fun testFindApprovedFile_no_match() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    val result = ReceivedFileUtil.findApprovedFile(received)

    assertNull(result)
  }

  fun testApproveAction_visible_with_approved_file() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    val presentation = updateAction(ApproveReceivedAction(), received)

    assertTrue(presentation.isEnabledAndVisible)
  }

  fun testApproveAction_hidden_without_approved_file() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    val presentation = updateAction(ApproveReceivedAction(), received)

    assertFalse(presentation.isEnabledAndVisible)
  }

  fun testCompareAction_visible_with_approved_file() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    val presentation = updateAction(CompareReceivedWithApprovedAction(), received)

    assertTrue(presentation.isEnabledAndVisible)
  }

  fun testCompareAction_hidden_without_approved_file() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    val presentation = updateAction(CompareReceivedWithApprovedAction(), received)

    assertFalse(presentation.isEnabledAndVisible)
  }

  fun testCompareAction_hidden_for_non_received_file() {
    myFixture.addFileToProject("MyTest.java", "class MyTest {}")
    val file = myFixture.findFileInTempDir("MyTest.java")

    val presentation = updateAction(CompareReceivedWithApprovedAction(), file)

    assertFalse(presentation.isEnabledAndVisible)
  }

  fun testIsActionAvailable_received_with_approved() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    assertTrue(ReceivedFileUtil.isActionAvailable(createEvent(received)))
  }

  fun testIsActionAvailable_received_without_approved() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    assertFalse(ReceivedFileUtil.isActionAvailable(createEvent(received)))
  }

  fun testIsActionAvailable_non_received_file() {
    myFixture.addFileToProject("MyTest.java", "class MyTest {}")
    val file = myFixture.findFileInTempDir("MyTest.java")

    assertFalse(ReceivedFileUtil.isActionAvailable(createEvent(file)))
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

    assertEquals("MyTest.byValue-received.txt", capturedReceived.get().name)
    assertEquals("MyTest.byValue-approved.txt", capturedApproved.get().name)
  }

  fun testWithReceivedAndApproved_does_nothing_without_approved() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")
    val capturedReceived = AtomicReference<VirtualFile>()

    ReceivedFileUtil.withReceivedAndApproved(createEvent(received)) { r, _ ->
      capturedReceived.set(r)
    }

    assertNull(capturedReceived.get())
  }

  fun testApprove_copies_content_and_deletes_received() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "new content")
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "old content")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")
    val approved = myFixture.findFileInTempDir("MyTest.byValue-approved.txt")

    ReceivedFileUtil.approve(project, received, approved)

    assertFalse(received.isValid)
    assertEquals("new content", String(approved.contentsToByteArray()))
  }

  fun testReject_deletes_received_without_changing_approved() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "new content")
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "old content")
    val received = myFixture.findFileInTempDir("MyTest.byValue-received.txt")
    val approved = myFixture.findFileInTempDir("MyTest.byValue-approved.txt")

    ReceivedFileUtil.reject(project, received)

    assertFalse(received.isValid)
    assertEquals("old content", String(approved.contentsToByteArray()))
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
