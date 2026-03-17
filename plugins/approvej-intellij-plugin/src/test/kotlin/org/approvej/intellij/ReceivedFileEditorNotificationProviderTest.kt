package org.approvej.intellij

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.EditorNotificationPanel
import org.assertj.core.api.Assertions.assertThat

class ReceivedFileEditorNotificationProviderTest : BasePlatformTestCase() {

  private val provider = ReceivedFileEditorNotificationProvider()

  fun testBanner_appears_for_received_file_with_approved() {
    myFixture.addFileToProject("MyTest.byValue-received.txt", "received content")
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved content")
    val file = myFixture.findFileInTempDir("MyTest.byValue-received.txt")

    val factory = provider.collectNotificationData(project, file)

    assertThat(factory).isNotNull()
  }

  fun testBanner_text_with_approved() {
    val psiFile = myFixture.addFileToProject("MyTest.byValue-received.txt", "received content")
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved content")
    val file = myFixture.findFileInTempDir("MyTest.byValue-received.txt")
    myFixture.openFileInEditor(psiFile.virtualFile)
    val editor = FileEditorManager.getInstance(project).selectedEditor!!

    val panel = provider.collectNotificationData(project, file)!!.apply(editor)

    assertThat((panel as EditorNotificationPanel).text)
      .isEqualTo("This is an ApproveJ received file that has not been approved yet.")
  }

  fun testBanner_text_without_approved() {
    val psiFile = myFixture.addFileToProject("MyTest.byValue-received.txt", "received content")
    val file = myFixture.findFileInTempDir("MyTest.byValue-received.txt")
    myFixture.openFileInEditor(psiFile.virtualFile)
    val editor = FileEditorManager.getInstance(project).selectedEditor!!

    val panel = provider.collectNotificationData(project, file)!!.apply(editor)

    assertThat((panel as EditorNotificationPanel).text)
      .isEqualTo("This is an ApproveJ received file. No matching approved file was found nearby.")
  }

  fun testBanner_absent_for_non_received_file() {
    myFixture.addFileToProject("MyTest.java", "class MyTest {}")
    val file = myFixture.findFileInTempDir("MyTest.java")

    val factory = provider.collectNotificationData(project, file)

    assertThat(factory).isNull()
  }

  fun testBanner_absent_for_approved_file() {
    myFixture.addFileToProject("MyTest.byValue-approved.txt", "approved content")
    val file = myFixture.findFileInTempDir("MyTest.byValue-approved.txt")

    val factory = provider.collectNotificationData(project, file)

    assertThat(factory).isNull()
  }
}
