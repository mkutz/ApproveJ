package org.approvej.intellij;

import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UastUtils;

public class ApproveCallUtilTest extends LightJavaCodeInsightFixtureTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.addClass(
        """
        package org.approvej;
        public class ApprovalBuilder<T> {
            public static <T> ApprovalBuilder<T> approve(T value) { return null; }
            public ApprovalBuilder<T> printedAs(Object format) { return this; }
            public void byFile() {}
            public void byValue(java.lang.String expected) {}
        }
        """);
  }

  public void testAsApproveCall() {
    PsiElement element =
        configureAndFindApproveIdentifier(
            """
            import static org.approvej.ApprovalBuilder.approve;
            class Test {
                void test() {
                    appr<caret>ove("hello").byFile();
                }
            }
            """);

    assertNotNull(ApproveCallUtil.asApproveCall(element));
  }

  public void testAsApproveCall_unrelated_method() {
    PsiElement element =
        configureAndFindApproveIdentifier(
            """
            class Test {
                void appr<caret>ove(String s) {}
            }
            """);

    assertNull(ApproveCallUtil.asApproveCall(element));
  }

  public void testIsApproveCall() {
    UCallExpression call =
        configureAndFindApproveCallExpression(
            """
            import static org.approvej.ApprovalBuilder.approve;
            class Test {
                void test() {
                    appr<caret>ove("hello").byFile();
                }
            }
            """);

    assertTrue(ApproveCallUtil.isApproveCall(call));
  }

  public void testFindTerminalCall_byFile() {
    UCallExpression call =
        configureAndFindApproveCallExpression(
            """
            import static org.approvej.ApprovalBuilder.approve;
            class Test {
                void test() {
                    appr<caret>ove("hello").byFile();
                }
            }
            """);

    assertEquals("byFile", ApproveCallUtil.findTerminalCall(call).lastMethodName());
  }

  public void testFindTerminalCall_byValue() {
    UCallExpression call =
        configureAndFindApproveCallExpression(
            """
            import static org.approvej.ApprovalBuilder.approve;
            class Test {
                void test() {
                    appr<caret>ove("hello").byValue("expected");
                }
            }
            """);

    assertEquals("byValue", ApproveCallUtil.findTerminalCall(call).lastMethodName());
  }

  public void testFindTerminalCall_with_intermediate() {
    UCallExpression call =
        configureAndFindApproveCallExpression(
            """
            import static org.approvej.ApprovalBuilder.approve;
            class Test {
                void test() {
                    appr<caret>ove("hello").printedAs(null).byFile();
                }
            }
            """);

    assertEquals("byFile", ApproveCallUtil.findTerminalCall(call).lastMethodName());
  }

  public void testFindTerminalCall_no_terminal() {
    UCallExpression call =
        configureAndFindApproveCallExpression(
            """
            import static org.approvej.ApprovalBuilder.approve;
            class Test {
                void test() {
                    appr<caret>ove("hello");
                }
            }
            """);

    assertEquals("approve", ApproveCallUtil.findTerminalCall(call).lastMethodName());
  }

  public void testFindTerminalCall_dangling_with_intermediate() {
    UCallExpression call =
        configureAndFindApproveCallExpression(
            """
            import static org.approvej.ApprovalBuilder.approve;
            class Test {
                void test() {
                    appr<caret>ove("hello").printedAs(null);
                }
            }
            """);

    assertEquals("printedAs", ApproveCallUtil.findTerminalCall(call).lastMethodName());
  }

  private PsiElement configureAndFindApproveIdentifier(String code) {
    myFixture.configureByText("Test.java", code);
    return myFixture.getFile().findElementAt(myFixture.getCaretOffset());
  }

  private UCallExpression configureAndFindApproveCallExpression(String code) {
    PsiElement element = configureAndFindApproveIdentifier(code);
    UElement uElement = UastUtils.getUParentForIdentifier(element);
    assertInstanceOf(uElement, UCallExpression.class);
    return (UCallExpression) uElement;
  }
}
