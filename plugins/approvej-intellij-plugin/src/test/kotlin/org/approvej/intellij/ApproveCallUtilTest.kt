package org.approvej.intellij

import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.getUParentForIdentifier

class ApproveCallUtilTest : LightJavaCodeInsightFixtureTestCase() {

  override fun setUp() {
    super.setUp()
    myFixture.addClass(
      """
      package org.approvej;
      public class ApprovalBuilder<T> {
          public static <T> ApprovalBuilder<T> approve(T value) { return null; }
          public ApprovalBuilder<T> printedAs(Object format) { return this; }
          public ApprovalBuilder<T> named(String name) { return this; }
          public void byFile() {}
          public void byValue(java.lang.String expected) {}
      }
      """
        .trimIndent()
    )
  }

  fun testAsApproveCall() {
    val element =
      configureAndFindApproveIdentifier(
        """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              appr<caret>ove("hello").byFile();
          }
      }
      """
          .trimIndent()
      )

    assertNotNull(ApproveCallUtil.asApproveCall(element))
  }

  fun testAsApproveCall_unrelated_method() {
    val element =
      configureAndFindApproveIdentifier(
        """
      class Test {
          void appr<caret>ove(String s) {}
      }
      """
          .trimIndent()
      )

    assertNull(ApproveCallUtil.asApproveCall(element))
  }

  fun testIsApproveCall() {
    val call =
      configureAndFindApproveCallExpression(
        """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              appr<caret>ove("hello").byFile();
          }
      }
      """
          .trimIndent()
      )

    assertTrue(ApproveCallUtil.isApproveCall(call))
  }

  fun testFindTerminalCall_byFile() {
    val call =
      configureAndFindApproveCallExpression(
        """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              appr<caret>ove("hello").byFile();
          }
      }
      """
          .trimIndent()
      )

    assertEquals("byFile", ApproveCallUtil.findTerminalCall(call).lastMethodName)
  }

  fun testFindTerminalCall_byValue() {
    val call =
      configureAndFindApproveCallExpression(
        """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              appr<caret>ove("hello").byValue("expected");
          }
      }
      """
          .trimIndent()
      )

    assertEquals("byValue", ApproveCallUtil.findTerminalCall(call).lastMethodName)
  }

  fun testFindTerminalCall_with_intermediate() {
    val call =
      configureAndFindApproveCallExpression(
        """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              appr<caret>ove("hello").printedAs(null).byFile();
          }
      }
      """
          .trimIndent()
      )

    assertEquals("byFile", ApproveCallUtil.findTerminalCall(call).lastMethodName)
  }

  fun testFindTerminalCall_no_terminal() {
    val call =
      configureAndFindApproveCallExpression(
        """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              appr<caret>ove("hello");
          }
      }
      """
          .trimIndent()
      )

    assertEquals("approve", ApproveCallUtil.findTerminalCall(call).lastMethodName)
  }

  fun testFindTerminalCall_dangling_with_intermediate() {
    val call =
      configureAndFindApproveCallExpression(
        """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              appr<caret>ove("hello").printedAs(null);
          }
      }
      """
          .trimIndent()
      )

    assertEquals("printedAs", ApproveCallUtil.findTerminalCall(call).lastMethodName)
  }

  fun testFindNamedArgument() {
    val call =
      configureAndFindApproveCallExpression(
        """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              appr<caret>ove("hello").named("first").byFile();
          }
      }
      """
          .trimIndent()
      )

    assertEquals("first", ApproveCallUtil.findNamedArgument(call))
  }

  fun testFindNamedArgument_with_intermediate() {
    val call =
      configureAndFindApproveCallExpression(
        """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              appr<caret>ove("hello").printedAs(null).named("second").byFile();
          }
      }
      """
          .trimIndent()
      )

    assertEquals("second", ApproveCallUtil.findNamedArgument(call))
  }

  fun testHasNamedCall() {
    val call =
      configureAndFindApproveCallExpression(
        """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              appr<caret>ove("hello").named("first").byFile();
          }
      }
      """
          .trimIndent()
      )

    assertTrue(ApproveCallUtil.hasNamedCall(call))
  }

  fun testHasNamedCall_absent() {
    val call =
      configureAndFindApproveCallExpression(
        """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              appr<caret>ove("hello").byFile();
          }
      }
      """
          .trimIndent()
      )

    assertFalse(ApproveCallUtil.hasNamedCall(call))
  }

  fun testFindNamedArgument_absent() {
    val call =
      configureAndFindApproveCallExpression(
        """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              appr<caret>ove("hello").byFile();
          }
      }
      """
          .trimIndent()
      )

    assertNull(ApproveCallUtil.findNamedArgument(call))
  }

  private fun configureAndFindApproveIdentifier(code: String): PsiElement {
    myFixture.configureByText("Test.java", code)
    return myFixture.file.findElementAt(myFixture.caretOffset)!!
  }

  private fun configureAndFindApproveCallExpression(code: String): UCallExpression {
    val element = configureAndFindApproveIdentifier(code)
    val uElement = getUParentForIdentifier(element)
    assertInstanceOf(uElement, UCallExpression::class.java)
    return uElement as UCallExpression
  }
}
