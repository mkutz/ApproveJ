package org.approvej.intellij;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

public class DanglingApprovalInspectionTest extends LightJavaCodeInsightFixtureTestCase {

  private static final String WARNING =
      "Dangling approval: call by(), byFile(), or byValue() to conclude";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(new DanglingApprovalInspection());
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
        """);
  }

  public void testApprove_alone() {
    doHighlightTest(
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                <warning descr="%s">approve("hello")</warning>;
            }
        }
        """
            .formatted(WARNING));
  }

  public void testApprove_with_intermediate_method() {
    doHighlightTest(
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                <warning descr="%s">approve("hello").printedAs(null)</warning>;
            }
        }
        """
            .formatted(WARNING));
  }

  public void testApprove_with_multiple_intermediate_methods() {
    doHighlightTest(
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                <warning descr="%s">approve("hello").printedAs(null).scrubbedOf(null)</warning>;
            }
        }
        """
            .formatted(WARNING));
  }

  public void testApprove_concluded_with_byFile() {
    doHighlightTest(
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                approve("hello").byFile();
            }
        }
        """);
  }

  public void testApprove_concluded_with_byValue() {
    doHighlightTest(
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                approve("hello").byValue("expected");
            }
        }
        """);
  }

  public void testApprove_concluded_with_by() {
    doHighlightTest(
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                approve("hello").by(null);
            }
        }
        """);
  }

  public void testApprove_with_intermediate_then_byFile() {
    doHighlightTest(
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                approve("hello").printedAs(null).byFile();
            }
        }
        """);
  }

  public void testApprove_assigned_to_variable() {
    doHighlightTest(
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                var builder = approve("hello");
            }
        }
        """);
  }

  public void testUnrelated_approve_method() {
    doHighlightTest(
        """
        class Test {
            void approve() {}
            void test() {
                approve();
            }
        }
        """);
  }

  public void testQuickfix_byFile_on_bare_approve() {
    doQuickFixTest(
        "Conclude with .byFile()",
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                appro<caret>ve("hello");
            }
        }
        """,
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                approve("hello").byFile();
            }
        }
        """);
  }

  public void testQuickfix_byFile_on_chain() {
    doQuickFixTest(
        "Conclude with .byFile()",
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                appro<caret>ve("hello").printedAs(null);
            }
        }
        """,
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                approve("hello").printedAs(null).byFile();
            }
        }
        """);
  }

  public void testQuickfix_byValue() {
    doQuickFixTest(
        "Conclude with .byValue(\"\")",
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                appro<caret>ve("hello");
            }
        }
        """,
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                approve("hello").byValue("");
            }
        }
        """);
  }

  private void doHighlightTest(String code) {
    myFixture.configureByText("Test.java", code);
    myFixture.checkHighlighting();
  }

  private void doQuickFixTest(String fixName, String before, String after) {
    myFixture.configureByText("Test.java", before);
    IntentionAction fix = myFixture.findSingleIntention(fixName);
    myFixture.launchAction(fix);
    myFixture.checkResult(after);
  }
}
