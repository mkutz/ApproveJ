package org.approvej.intellij;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class ReceivedFileUtilPlatformTest extends BasePlatformTestCase {

  public void testFindApprovedFile_approved_infix() throws Exception {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received");
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved");
    VirtualFile received = myFixture.findFileInTempDir("MyTest.byValue-received.txt");

    VirtualFile result = ReceivedFileUtil.findApprovedFile(received);

    assertNotNull(result);
    assertEquals("MyTest.byValue-approved.txt", result.getName());
  }

  public void testFindApprovedFile_custom_base_name() throws Exception {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received");
    myFixture.addFileToProject("MyTest.byValue.txt", "approved");
    VirtualFile received = myFixture.findFileInTempDir("MyTest.byValue-received.txt");

    VirtualFile result = ReceivedFileUtil.findApprovedFile(received);

    assertNotNull(result);
    assertEquals("MyTest.byValue.txt", result.getName());
  }

  public void testFindApprovedFile_prefers_approved_infix() throws Exception {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received");
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved");
    myFixture.addFileToProject("MyTest.byValue.txt", "base");
    VirtualFile received = myFixture.findFileInTempDir("MyTest.byValue-received.txt");

    VirtualFile result = ReceivedFileUtil.findApprovedFile(received);

    assertNotNull(result);
    assertEquals("MyTest.byValue-approved.txt", result.getName());
  }

  public void testFindApprovedFile_no_match() throws Exception {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received");
    VirtualFile received = myFixture.findFileInTempDir("MyTest.byValue-received.txt");

    VirtualFile result = ReceivedFileUtil.findApprovedFile(received);

    assertNull(result);
  }
}
