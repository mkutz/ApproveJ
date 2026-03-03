package org.approvej.intellij;

import com.intellij.openapi.actionSystem.ActionUiKind;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class ReceivedFileUtilPlatformTest extends BasePlatformTestCase {

  public void testFindApprovedFile_approved_infix() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received");
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved");
    VirtualFile received = myFixture.findFileInTempDir("MyTest.byValue-received.txt");

    VirtualFile result = ReceivedFileUtil.findApprovedFile(received);

    assertNotNull(result);
    assertEquals("MyTest.byValue-approved.txt", result.getName());
  }

  public void testFindApprovedFile_custom_base_name() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received");
    myFixture.addFileToProject("MyTest.byValue.txt", "approved");
    VirtualFile received = myFixture.findFileInTempDir("MyTest.byValue-received.txt");

    VirtualFile result = ReceivedFileUtil.findApprovedFile(received);

    assertNotNull(result);
    assertEquals("MyTest.byValue.txt", result.getName());
  }

  public void testFindApprovedFile_prefers_approved_infix() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received");
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved");
    myFixture.addFileToProject("MyTest.byValue.txt", "base");
    VirtualFile received = myFixture.findFileInTempDir("MyTest.byValue-received.txt");

    VirtualFile result = ReceivedFileUtil.findApprovedFile(received);

    assertNotNull(result);
    assertEquals("MyTest.byValue-approved.txt", result.getName());
  }

  public void testFindApprovedFile_no_match() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received");
    VirtualFile received = myFixture.findFileInTempDir("MyTest.byValue-received.txt");

    VirtualFile result = ReceivedFileUtil.findApprovedFile(received);

    assertNull(result);
  }

  public void testApproveAction_visible_with_approved_file() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received");
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved");
    VirtualFile received = myFixture.findFileInTempDir("MyTest.byValue-received.txt");

    Presentation presentation = updateAction(new ApproveReceivedAction(), received);

    assertTrue(presentation.isEnabledAndVisible());
  }

  public void testApproveAction_hidden_without_approved_file() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received");
    VirtualFile received = myFixture.findFileInTempDir("MyTest.byValue-received.txt");

    Presentation presentation = updateAction(new ApproveReceivedAction(), received);

    assertFalse(presentation.isEnabledAndVisible());
  }

  public void testCompareAction_visible_with_approved_file() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received");
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved");
    VirtualFile received = myFixture.findFileInTempDir("MyTest.byValue-received.txt");

    Presentation presentation = updateAction(new CompareReceivedWithApprovedAction(), received);

    assertTrue(presentation.isEnabledAndVisible());
  }

  public void testCompareAction_hidden_without_approved_file() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received");
    VirtualFile received = myFixture.findFileInTempDir("MyTest.byValue-received.txt");

    Presentation presentation = updateAction(new CompareReceivedWithApprovedAction(), received);

    assertFalse(presentation.isEnabledAndVisible());
  }

  public void testCompareAction_hidden_for_non_received_file() {
    myFixture.addFileToProject("MyTest.java", "class MyTest {}");
    VirtualFile file = myFixture.findFileInTempDir("MyTest.java");

    Presentation presentation = updateAction(new CompareReceivedWithApprovedAction(), file);

    assertFalse(presentation.isEnabledAndVisible());
  }

  public void testApprove_copies_content_and_deletes_received() throws Exception {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "new content");
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "old content");
    VirtualFile received = myFixture.findFileInTempDir("MyTest.byValue-received.txt");
    VirtualFile approved = myFixture.findFileInTempDir("MyTest.byValue-approved.txt");

    ReceivedFileUtil.approve(getProject(), received, approved);

    assertFalse(received.isValid());
    assertEquals("new content", new String(approved.contentsToByteArray()));
  }

  private Presentation updateAction(AnAction action, VirtualFile file) {
    var dataContext =
        SimpleDataContext.builder()
            .add(CommonDataKeys.VIRTUAL_FILE, file)
            .add(CommonDataKeys.PROJECT, getProject())
            .build();
    var event = AnActionEvent.createEvent(dataContext, null, "test", ActionUiKind.NONE, null);
    ActionUtil.performDumbAwareUpdate(action, event, false);
    return event.getPresentation();
  }
}
