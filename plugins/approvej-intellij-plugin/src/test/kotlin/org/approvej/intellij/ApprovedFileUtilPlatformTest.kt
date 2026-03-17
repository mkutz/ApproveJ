package org.approvej.intellij

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ApprovedFileUtilPlatformTest : BasePlatformTestCase() {

  fun testFindReceivedFile() {
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved")
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received")
    val approved = myFixture.findFileInTempDir("MyTest.byValue-approved.txt")

    val result = ApprovedFileUtil.findReceivedFile(approved)

    assertNotNull(result)
    assertEquals("MyTest.byValue-received.txt", result!!.name)
  }

  fun testFindReceivedFile_no_extension() {
    myFixture.addFileToProject("MyTest.byValue-approved", "approved")
    myFixture.addFileToProject("MyTest.byValue-received", "received")
    val approved = myFixture.findFileInTempDir("MyTest.byValue-approved")

    val result = ApprovedFileUtil.findReceivedFile(approved)

    assertNotNull(result)
    assertEquals("MyTest.byValue-received", result!!.name)
  }

  fun testFindReceivedFile_with_affix() {
    myFixture.addFileToProject("MyTest.byValue-body-approved.json", "approved")
    myFixture.addFileToProject("MyTest.byValue-body-received.json", "received")
    val approved = myFixture.findFileInTempDir("MyTest.byValue-body-approved.json")

    val result = ApprovedFileUtil.findReceivedFile(approved)

    assertNotNull(result)
    assertEquals("MyTest.byValue-body-received.json", result!!.name)
  }

  fun testFindReceivedFile_no_received_file() {
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved")
    val approved = myFixture.findFileInTempDir("MyTest.byValue-approved.txt")

    val result = ApprovedFileUtil.findReceivedFile(approved)

    assertNull(result)
  }

  fun testFindReceivedFile_non_approved_file() {
    myFixture.addFileToProject("MyTest.java", "class MyTest {}")
    val file = myFixture.findFileInTempDir("MyTest.java")

    val result = ApprovedFileUtil.findReceivedFile(file)

    assertNull(result)
  }
}
