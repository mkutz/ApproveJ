package org.approvej.intellij;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.util.function.Function;
import javax.swing.JComponent;

public class ApprovedFileEditorNotificationProviderTest extends BasePlatformTestCase {

  private final ApprovedFileEditorNotificationProvider provider =
      new ApprovedFileEditorNotificationProvider();

  public void testBanner_appears_for_approved_file() {
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved content");
    VirtualFile file = myFixture.findFileInTempDir("MyTest.byValue-approved.txt");

    Function<? super FileEditor, ? extends JComponent> factory =
        provider.collectNotificationData(getProject(), file);

    assertNotNull(factory);
  }

  public void testBanner_absent_for_non_approved_file() {
    myFixture.addFileToProject("MyTest.java", "class MyTest {}");
    VirtualFile file = myFixture.findFileInTempDir("MyTest.java");

    Function<? super FileEditor, ? extends JComponent> factory =
        provider.collectNotificationData(getProject(), file);

    assertNull(factory);
  }

  public void testBanner_absent_for_received_file() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received content");
    VirtualFile file = myFixture.findFileInTempDir("MyTest.byValue-received.txt");

    Function<? super FileEditor, ? extends JComponent> factory =
        provider.collectNotificationData(getProject(), file);

    assertNull(factory);
  }
}
