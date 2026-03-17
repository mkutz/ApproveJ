package org.approvej.intellij

import com.intellij.codeInsight.daemon.GutterMark
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import org.assertj.core.api.Assertions.assertThat

class ApproveCallLineMarkerProviderTest : LightJavaCodeInsightFixtureTestCase() {

  override fun setUp() {
    super.setUp()
    myFixture.addClass(
      """
      package org.approvej;
      public class ApprovalBuilder<T> {
          public static <T> ApprovalBuilder<T> approve(T value) { return null; }
          public ApprovalBuilder<T> printedAs(Object format) { return this; }
          public ApprovalBuilder<T> scrubbedOf(Object scrubber) { return this; }
          public ApprovalBuilder<T> named(String name) { return this; }
          public void byFile() {}
          public void byValue(java.lang.String expected) {}
          public Object by(Object approver) { return null; }
      }
      """
        .trimIndent()
    )
  }

  fun testMarker_absent_for_byValue() {
    addInventory("src/com/example/Test-test-approved.txt", "com.example.Test#test")
    myFixture.addFileToProject("src/com/example/Test-test-approved.txt", "approved content")

    myFixture.configureByText(
      "Test.java",
      """
      package com.example;
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              approve("hello").byValue("expected");
          }
      }
      """
        .trimIndent(),
    )

    assertThat(findApprovalGutters()).isEmpty()
  }

  fun testMarker_absent_for_unrelated_approve() {
    myFixture.configureByText(
      "Test.java",
      """
      class Test {
          void approve(String s) {}
          void test() {
              approve("hello");
          }
      }
      """
        .trimIndent(),
    )

    assertThat(myFixture.findAllGutters()).noneMatch {
      it.tooltipText?.contains("approved") == true
    }
  }

  fun testMarker_present_for_byFile() {
    addInventory("src/com/example/Test-myTest-approved.txt", "com.example.Test#myTest")
    myFixture.addFileToProject("src/com/example/Test-myTest-approved.txt", "approved content")

    myFixture.configureByText(
      "Test.java",
      """
      package com.example;
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void myTest() {
              approve("x").byFile();
          }
      }
      """
        .trimIndent(),
    )

    val gutters = findApprovalGutters()
    assertThat(gutters).hasSize(1)
    assertThat(gutters.first().tooltipText).isEqualTo("Navigate to Test-myTest-approved.txt")
  }

  fun testMarker_named_filters_to_matching_file() {
    addInventory(
      "src/com/example/Test-myTest-jane-approved.txt",
      "com.example.Test#myTest",
      "src/com/example/Test-myTest-john-approved.txt",
      "com.example.Test#myTest",
    )
    myFixture.addFileToProject("src/com/example/Test-myTest-jane-approved.txt", "jane content")
    myFixture.addFileToProject("src/com/example/Test-myTest-john-approved.txt", "john content")

    myFixture.configureByText(
      "Test.java",
      """
      package com.example;
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void myTest() {
              approve("x").named("jane").byFile();
          }
      }
      """
        .trimIndent(),
    )

    val gutters = findApprovalGutters()
    assertThat(gutters).hasSize(1)
    assertThat(gutters.first().tooltipText).contains("jane").doesNotContain("john")
  }

  fun testMarker_unnamed_excludes_named_files() {
    addInventory(
      "src/com/example/Test-myTest-approved.txt",
      "com.example.Test#myTest",
      "src/com/example/Test-myTest-jane-approved.txt",
      "com.example.Test#myTest",
    )
    myFixture.addFileToProject("src/com/example/Test-myTest-approved.txt", "unnamed content")
    myFixture.addFileToProject("src/com/example/Test-myTest-jane-approved.txt", "jane content")

    myFixture.configureByText(
      "Test.java",
      """
      package com.example;
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void myTest() {
              approve("x").byFile();
          }
      }
      """
        .trimIndent(),
    )

    val gutters = findApprovalGutters()
    assertThat(gutters).hasSize(1)
    assertThat(gutters.first().tooltipText).isEqualTo("Navigate to Test-myTest-approved.txt")
  }

  fun testMarker_multiple_named_approvals() {
    addInventory(
      "src/com/example/Test-myTest-jane-approved.txt",
      "com.example.Test#myTest",
      "src/com/example/Test-myTest-john-approved.txt",
      "com.example.Test#myTest",
    )
    myFixture.addFileToProject("src/com/example/Test-myTest-jane-approved.txt", "jane content")
    myFixture.addFileToProject("src/com/example/Test-myTest-john-approved.txt", "john content")

    myFixture.configureByText(
      "Test.java",
      """
      package com.example;
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void myTest() {
              approve("x").named("jane").byFile();
              approve("x").named("john").byFile();
          }
      }
      """
        .trimIndent(),
    )

    val gutters = findApprovalGutters()
    assertThat(gutters).hasSize(2)
    assertThat(gutters[0].tooltipText).isNotEqualTo(gutters[1].tooltipText)
  }

  fun testMarker_named_approval_shown_when_sibling_unapproved() {
    addInventory(
      "src/com/example/Test-myTest-a-approved.txt",
      "com.example.Test#myTest",
      "src/com/example/Test-myTest-b-approved.txt",
      "com.example.Test#myTest",
    )
    myFixture.addFileToProject("src/com/example/Test-myTest-a-approved.txt", "a content")
    myFixture.addFileToProject("src/com/example/Test-myTest-a-received.txt", "a different content")
    myFixture.addFileToProject("src/com/example/Test-myTest-b-approved.txt", "b content")

    myFixture.configureByText(
      "Test.java",
      """
      package com.example;
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void myTest() {
              approve("x").named("a").byFile();
              approve("y").named("b").byFile();
          }
      }
      """
        .trimIndent(),
    )

    val gutters = findApprovalGutters()
    assertThat(gutters).hasSize(2)
    assertThat(gutters[0].tooltipText).isEqualTo("Compare received with approved")
    assertThat(gutters[1].tooltipText).isEqualTo("Navigate to Test-myTest-b-approved.txt")
  }

  fun testMarker_received_file_changes_icon() {
    addInventory("src/com/example/Test-myTest-approved.txt", "com.example.Test#myTest")
    myFixture.addFileToProject("src/com/example/Test-myTest-approved.txt", "approved content")
    myFixture.addFileToProject("src/com/example/Test-myTest-received.txt", "received content")

    myFixture.configureByText(
      "Test.java",
      """
      package com.example;
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void myTest() {
              approve("x").byFile();
          }
      }
      """
        .trimIndent(),
    )

    val gutters = findApprovalGutters()
    assertThat(gutters).hasSize(1)
    assertThat(gutters.first().tooltipText).isEqualTo("Compare received with approved")
  }

  private fun findApprovalGutters(): List<GutterMark> =
    myFixture.findAllGutters().filter { g ->
      val tooltip = g.tooltipText
      tooltip != null && (tooltip.contains("approved") || tooltip.contains("received"))
    }

  private fun addInventory(vararg keysAndValues: String) {
    val sb = StringBuilder("# ApproveJ Approved File Inventory\n")
    var i = 0
    while (i < keysAndValues.size) {
      sb
        .append(keysAndValues[i].replace(" ", "\\ "))
        .append(" = ")
        .append(keysAndValues[i + 1])
        .append("\n")
      i += 2
    }
    myFixture.addFileToProject(".approvej/inventory.properties", sb.toString())
  }
}
