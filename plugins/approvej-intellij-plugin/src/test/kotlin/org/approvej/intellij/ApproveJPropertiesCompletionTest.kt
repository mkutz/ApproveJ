package org.approvej.intellij

import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import org.assertj.core.api.Assertions.assertThat

class ApproveJPropertiesCompletionTest : LightJavaCodeInsightFixtureTestCase() {

  fun testKeyCompletion() {
    myFixture.configureByText("approvej.properties", "<caret>")
    val completions = myFixture.completeBasic()
    val keys = completions.map { it.lookupString }
    assertThat(keys)
      .contains(
        "defaultPrintFormat",
        "defaultFileReviewer",
        "reviewerScript",
        "reviewerAiCommand",
        "inventoryEnabled",
        "defaultInlineValueReviewer",
        "defaultFileReviewerScript",
      )
  }

  fun testKeyCompletion_prefix() {
    myFixture.configureByText("approvej.properties", "default<caret>")
    val completions = myFixture.completeBasic()
    val keys = completions.map { it.lookupString }
    assertThat(keys)
      .contains("defaultPrintFormat", "defaultFileReviewer", "defaultInlineValueReviewer")
    assertThat(keys).doesNotContain("reviewerScript", "inventoryEnabled")
  }

  fun testValueCompletion_printFormat() {
    myFixture.configureByText("approvej.properties", "defaultPrintFormat = <caret>")
    val completions = myFixture.completeBasic()
    val values = completions.map { it.lookupString }
    assertThat(values).contains("singleLineString", "multiLineString", "json", "yaml")
  }

  fun testValueCompletion_fileReviewer() {
    myFixture.configureByText("approvej.properties", "defaultFileReviewer = <caret>")
    val completions = myFixture.completeBasic()
    val values = completions.map { it.lookupString }
    assertThat(values).contains("none", "automatic", "script", "ai")
  }

  fun testValueCompletion_inventoryEnabled() {
    myFixture.configureByText("approvej.properties", "inventoryEnabled = <caret>")
    val completions = myFixture.completeBasic()
    val values = completions.map { it.lookupString }
    assertThat(values).contains("true", "false")
  }

  fun testValueCompletion_freeFormKey() {
    myFixture.configureByText("approvej.properties", "reviewerScript = <caret>")
    val completions = myFixture.completeBasic()
    assertThat(completions).isNullOrEmpty()
  }

  fun testWrongFileName() {
    myFixture.configureByText("other.properties", "<caret>")
    val completions = myFixture.completeBasic()
    val keys = completions?.map { it.lookupString } ?: emptyList()
    assertThat(keys).doesNotContain("defaultPrintFormat")
  }
}
