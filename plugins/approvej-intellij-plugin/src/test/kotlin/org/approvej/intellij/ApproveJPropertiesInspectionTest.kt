package org.approvej.intellij

import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

class ApproveJPropertiesInspectionTest : LightJavaCodeInsightFixtureTestCase() {

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(ApproveJPropertiesInspection())
  }

  fun testKnownKey() {
    doHighlightTest("defaultPrintFormat = json")
  }

  fun testKnownKey_inventoryEnabled() {
    doHighlightTest("inventoryEnabled = true")
  }

  fun testAllKnownKeys() {
    doHighlightTest(
      """
      defaultPrintFormat = singleLineString
      defaultFileReviewer = none
      reviewerScript = idea diff
      reviewerAiCommand = claude -p
      inventoryEnabled = false
      defaultInlineValueReviewer = automatic
      """
        .trimIndent()
    )
  }

  fun testUnknownKey() {
    doHighlightTest(
      "<warning descr=\"Unknown ApproveJ property key 'misspelledKey'\">misspelledKey</warning> = json"
    )
  }

  fun testUnknownKey_similar_to_valid() {
    doHighlightTest(
      "<warning descr=\"Unknown ApproveJ property key 'defaultprintformat'\">defaultprintformat</warning> = json"
    )
  }

  fun testDeprecatedKey() {
    doHighlightTest(
      "<warning descr=\"'defaultFileReviewerScript' is deprecated." +
        " Use 'defaultFileReviewer = script' and 'reviewerScript = ...' instead.\">" +
        "defaultFileReviewerScript</warning> = someScript.sh"
    )
  }

  fun testMixedKeys() {
    doHighlightTest(
      """
      defaultPrintFormat = json
      <warning descr="Unknown ApproveJ property key 'typo'">typo</warning> = value
      <warning descr="'defaultFileReviewerScript' is deprecated. Use 'defaultFileReviewer = script' and 'reviewerScript = ...' instead.">defaultFileReviewerScript</warning> = script.sh
      """
        .trimIndent()
    )
  }

  fun testWrongFileName() {
    myFixture.configureByText("other.properties", "misspelledKey = json")
    myFixture.checkHighlighting()
  }

  private fun doHighlightTest(content: String) {
    myFixture.configureByText("approvej.properties", content)
    myFixture.checkHighlighting()
  }
}
