package org.approvej.intellij

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.EditorNotificationPanel

class ApprovedFileEditorNotificationProviderTest : BasePlatformTestCase() {

  private val provider = ApprovedFileEditorNotificationProvider()

  fun testBanner_appears_for_approved_file() {
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved content")
    val file = myFixture.findFileInTempDir("MyTest.byValue-approved.txt")

    val factory = provider.collectNotificationData(project, file)

    assertNotNull(factory)
  }

  fun testBanner_text_for_approved_file() {
    val psiFile = myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved content")
    val file = myFixture.findFileInTempDir("MyTest.byValue-approved.txt")
    myFixture.openFileInEditor(psiFile.virtualFile)
    val editor = FileEditorManager.getInstance(project).selectedEditor!!

    val panel = provider.collectNotificationData(project, file)!!.apply(editor)

    assertEquals("This is an ApproveJ approved file.", (panel as EditorNotificationPanel).text)
  }

  fun testBanner_absent_for_non_approved_file() {
    myFixture.addFileToProject("MyTest.java", "class MyTest {}")
    val file = myFixture.findFileInTempDir("MyTest.java")

    val factory = provider.collectNotificationData(project, file)

    assertNull(factory)
  }

  fun testBanner_absent_for_received_file() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received content")
    val file = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    val factory = provider.collectNotificationData(project, file)

    assertNull(factory)
  }
}
