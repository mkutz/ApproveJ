package org.approvej.intellij

import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

class DanglingApprovalInspectionTest : LightJavaCodeInsightFixtureTestCase() {

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(DanglingApprovalInspection())
    myFixture.addClass(
      """
      package org.approvej;
      public class ApprovalBuilder<T> {
          public static <T> ApprovalBuilder<T> approve(T value) { return null; }
          public ApprovalBuilder<T> printedAs(Object format) { return this; }
          public ApprovalBuilder<T> scrubbedOf(Object scrubber) { return this; }
          public void byFile() {}
          public void byValue(java.lang.String expected) {}
          public Object by(Object approver) { return null; }
      }
      """
        .trimIndent()
    )
    myFixture.addClass(
      """
      package org.approvej.image;
      import java.awt.image.BufferedImage;
      public class ImageApprovalBuilder {
          public static ImageApprovalBuilder approveImage(BufferedImage value) { return null; }
          public ImageApprovalBuilder named(String name) { return this; }
          public void byFile() {}

      }
      """
        .trimIndent()
    )
  }

  fun testApproveImage_alone() {
    doHighlightTest(
      """
      import static org.approvej.image.ImageApprovalBuilder.approveImage;
      class Test {
          void test() {
              <warning descr="$WARNING">approveImage(null)</warning>;
          }
      }
      """
        .trimIndent()
    )
  }

  fun testApprove_alone() {
    doHighlightTest(
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              <warning descr="$WARNING">approve("hello")</warning>;
          }
      }
      """
        .trimIndent()
    )
  }

  fun testApprove_with_intermediate_method() {
    doHighlightTest(
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              <warning descr="$WARNING">approve("hello").printedAs(null)</warning>;
          }
      }
      """
        .trimIndent()
    )
  }

  fun testApprove_with_multiple_intermediate_methods() {
    doHighlightTest(
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              <warning descr="$WARNING">approve("hello").printedAs(null).scrubbedOf(null)</warning>;
          }
      }
      """
        .trimIndent()
    )
  }

  fun testApprove_concluded_with_byFile() {
    doHighlightTest(
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              approve("hello").byFile();
          }
      }
      """
        .trimIndent()
    )
  }

  fun testApprove_concluded_with_byValue() {
    doHighlightTest(
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              approve("hello").byValue("expected");
          }
      }
      """
        .trimIndent()
    )
  }

  fun testApprove_concluded_with_by() {
    doHighlightTest(
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              approve("hello").by(null);
          }
      }
      """
        .trimIndent()
    )
  }

  fun testApprove_with_intermediate_then_byFile() {
    doHighlightTest(
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              approve("hello").printedAs(null).byFile();
          }
      }
      """
        .trimIndent()
    )
  }

  fun testApprove_assigned_to_variable() {
    doHighlightTest(
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              var builder = approve("hello");
          }
      }
      """
        .trimIndent()
    )
  }

  fun testUnrelated_approve_method() {
    doHighlightTest(
      """
      class Test {
          void approve() {}
          void test() {
              approve();
          }
      }
      """
        .trimIndent()
    )
  }

  fun testQuickfix_byFile_on_bare_approve() {
    doQuickFixTest(
      "Conclude with .byFile()",
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              appro<caret>ve("hello");
          }
      }
      """
        .trimIndent(),
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              approve("hello").byFile();
          }
      }
      """
        .trimIndent(),
    )
  }

  fun testQuickfix_byFile_on_chain() {
    doQuickFixTest(
      "Conclude with .byFile()",
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              appro<caret>ve("hello").printedAs(null);
          }
      }
      """
        .trimIndent(),
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              approve("hello").printedAs(null).byFile();
          }
      }
      """
        .trimIndent(),
    )
  }

  fun testQuickfix_byValue() {
    doQuickFixTest(
      """Conclude with .byValue("")""",
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              appro<caret>ve("hello");
          }
      }
      """
        .trimIndent(),
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              approve("hello").byValue("");
          }
      }
      """
        .trimIndent(),
    )
  }

  private fun doHighlightTest(code: String) {
    myFixture.configureByText("Test.java", code)
    myFixture.checkHighlighting()
  }

  private fun doQuickFixTest(fixName: String, before: String, after: String) {
    myFixture.configureByText("Test.java", before)
    val fix = myFixture.findSingleIntention(fixName)
    myFixture.launchAction(fix)
    myFixture.checkResult(after)
  }

  companion object {
    private const val WARNING = "Dangling approval: call by(), byFile(), or byValue() to conclude"
  }
}
