package org.approvej.intellij

import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import org.assertj.core.api.Assertions.assertThat
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

    assertThat(ApproveCallUtil.asApproveCall(element)).isNotNull()
  }

  fun testAsApproveCall_image() {
    val element =
      configureAndFindApproveIdentifier(
        """
      import static org.approvej.image.ImageApprovalBuilder.approveImage;
      class Test {
          void test() {
              appr<caret>oveImage(null).byFile();
          }
      }
      """
          .trimIndent()
      )

    assertThat(ApproveCallUtil.asApproveCall(element)).isNotNull()
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

    assertThat(ApproveCallUtil.asApproveCall(element)).isNull()
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

    assertThat(ApproveCallUtil.isApproveCall(call)).isTrue()
  }

  fun testIsApproveCall_image() {
    val call =
      configureAndFindApproveCallExpression(
        """
      import static org.approvej.image.ImageApprovalBuilder.approveImage;
      class Test {
          void test() {
              appr<caret>oveImage(null).byFile();
          }
      }
      """
          .trimIndent()
      )

    assertThat(ApproveCallUtil.isApproveCall(call)).isTrue()
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

    assertThat(ApproveCallUtil.findTerminalCall(call).lastMethodName).isEqualTo("byFile")
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

    assertThat(ApproveCallUtil.findTerminalCall(call).lastMethodName).isEqualTo("byValue")
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

    assertThat(ApproveCallUtil.findTerminalCall(call).lastMethodName).isEqualTo("byFile")
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

    assertThat(ApproveCallUtil.findTerminalCall(call).lastMethodName).isEqualTo("approve")
  }

  fun testFindTerminalCall_no_terminal_image() {
    val call =
      configureAndFindApproveCallExpression(
        """
      import static org.approvej.image.ImageApprovalBuilder.approveImage;
      class Test {
          void test() {
              appr<caret>oveImage(null);
          }
      }
      """
          .trimIndent()
      )

    assertThat(ApproveCallUtil.findTerminalCall(call).lastMethodName).isEqualTo("approveImage")
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

    assertThat(ApproveCallUtil.findTerminalCall(call).lastMethodName).isEqualTo("printedAs")
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

    assertThat(ApproveCallUtil.findNamedArgument(call)).isEqualTo("first")
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

    assertThat(ApproveCallUtil.findNamedArgument(call)).isEqualTo("second")
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

    assertThat(ApproveCallUtil.hasNamedCall(call)).isTrue()
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

    assertThat(ApproveCallUtil.hasNamedCall(call)).isFalse()
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

    assertThat(ApproveCallUtil.findNamedArgument(call)).isNull()
  }

  private fun configureAndFindApproveIdentifier(code: String): PsiElement {
    myFixture.configureByText("Test.java", code)
    return myFixture.file.findElementAt(myFixture.caretOffset)!!
  }

  private fun configureAndFindApproveCallExpression(code: String): UCallExpression {
    val element = configureAndFindApproveIdentifier(code)
    val uElement = getUParentForIdentifier(element)
    assertThat(uElement).isInstanceOf(UCallExpression::class.java)
    return uElement as UCallExpression
  }
}
