package org.approvej.intellij;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class ApprovedFileUtilPlatformTest extends BasePlatformTestCase {

  public void testFindReceivedFile() {
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved");
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received");
    VirtualFile approved = myFixture.findFileInTempDir("MyTest.byValue-approved.txt");

    VirtualFile result = ApprovedFileUtil.findReceivedFile(approved);

    assertNotNull(result);
    assertEquals("MyTest.byValue-received.txt", result.getName());
  }

  public void testFindReceivedFile_no_extension() {
    myFixture.addFileToProject("MyTest.byValue-approved", "approved");
    myFixture.addFileToProject("MyTest.byValue-received", "received");
    VirtualFile approved = myFixture.findFileInTempDir("MyTest.byValue-approved");

    VirtualFile result = ApprovedFileUtil.findReceivedFile(approved);

    assertNotNull(result);
    assertEquals("MyTest.byValue-received", result.getName());
  }

  public void testFindReceivedFile_with_affix() {
    myFixture.addFileToProject("MyTest.byValue-body-approved.json", "approved");
    myFixture.addFileToProject("MyTest.byValue-body-received.json", "received");
    VirtualFile approved = myFixture.findFileInTempDir("MyTest.byValue-body-approved.json");

    VirtualFile result = ApprovedFileUtil.findReceivedFile(approved);

    assertNotNull(result);
    assertEquals("MyTest.byValue-body-received.json", result.getName());
  }

  public void testFindReceivedFile_no_received_file() {
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved");
    VirtualFile approved = myFixture.findFileInTempDir("MyTest.byValue-approved.txt");

    VirtualFile result = ApprovedFileUtil.findReceivedFile(approved);

    assertNull(result);
  }

  public void testFindReceivedFile_non_approved_file() {
    myFixture.addFileToProject("MyTest.java", "class MyTest {}");
    VirtualFile file = myFixture.findFileInTempDir("MyTest.java");

    VirtualFile result = ApprovedFileUtil.findReceivedFile(file);

    assertNull(result);
  }
}
