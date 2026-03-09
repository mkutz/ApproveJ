package org.approvej.intellij;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

public class DuplicateUnnamedApprovalInspectionTest extends LightJavaCodeInsightFixtureTestCase {

  private static final String UNNAMED_WARNING =
      "Duplicate unnamed approval: each subsequent byFile() overwrites the previous approved file."
          + " Use .named() to distinguish.";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(new DuplicateUnnamedApprovalInspection());
    myFixture.addClass(
        """
        package org.approvej;
        public class ApprovalBuilder<T> {
            public static <T> ApprovalBuilder<T> approve(T value) { return null; }
            public ApprovalBuilder<T> printedAs(Object format) { return this; }
            public ApprovalBuilder<T> scrubbedOf(Object scrubber) { return this; }
            public ApprovalBuilder<T> named(String name) { return this; }
            public void byFile() {}
            public void byFile(String path) {}
            public void byValue(java.lang.String expected) {}
            public Object by(Object approver) { return null; }
        }
        """);
  }

  public void testTwo_unnamed_byFile_calls() {
    doHighlightTest(
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                <warning descr="%s">approve("a").byFile()</warning>;
                <warning descr="%s">approve("b").byFile()</warning>;
            }
        }
        """
            .formatted(UNNAMED_WARNING, UNNAMED_WARNING));
  }

  public void testThree_unnamed_byFile_calls() {
    doHighlightTest(
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                <warning descr="%s">approve("a").byFile()</warning>;
                <warning descr="%s">approve("b").byFile()</warning>;
                <warning descr="%s">approve("c").byFile()</warning>;
            }
        }
        """
            .formatted(UNNAMED_WARNING, UNNAMED_WARNING, UNNAMED_WARNING));
  }

  public void testSingle_unnamed_byFile_call() {
    doHighlightTest(
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                approve("a").byFile();
            }
        }
        """);
  }

  public void testTwo_byFile_calls_with_different_names() {
    doHighlightTest(
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                approve("a").named("first").byFile();
                approve("b").named("second").byFile();
            }
        }
        """);
  }

  public void testTwo_byFile_calls_one_named_one_unnamed() {
    doHighlightTest(
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                approve("a").named("first").byFile();
                approve("b").byFile();
            }
        }
        """);
  }

  public void testTwo_byFile_calls_with_same_name() {
    String warning =
        "Duplicate approval name 'foo': each subsequent byFile() overwrites the previous"
            + " approved file. Use distinct names.";
    doHighlightTest(
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                <warning descr="%s">approve("a").named("foo").byFile()</warning>;
                <warning descr="%s">approve("b").named("foo").byFile()</warning>;
            }
        }
        """
            .formatted(warning, warning));
  }

  public void testNamed_with_non_constant_arg_excluded() {
    myFixture.addClass(
        """
        package org.example;
        public class Names {
            public static String get() { return "dynamic"; }
        }
        """);
    doHighlightTest(
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                approve("a").named(org.example.Names.get()).byFile();
                approve("b").byFile();
            }
        }
        """);
  }

  public void testByFile_with_args_excluded() {
    doHighlightTest(
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                approve("a").byFile("/some/path");
                approve("b").byFile("/other/path");
            }
        }
        """);
  }

  public void testTwo_byValue_calls() {
    doHighlightTest(
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                approve("a").byValue("expected1");
                approve("b").byValue("expected2");
            }
        }
        """);
  }

  public void testMixed_byFile_and_byValue() {
    doHighlightTest(
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                approve("a").byFile();
                approve("b").byValue("expected");
            }
        }
        """);
  }

  public void testQuickfix_add_named_to_single() {
    myFixture.configureByText(
        "Test.java",
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                appro<caret>ve("a").byFile();
                approve("b").byFile();
            }
        }
        """);
    IntentionAction fix = myFixture.findSingleIntention("Add .named(\"TODO\")");
    myFixture.launchAction(fix);
    myFixture.checkResult(
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                approve("a").named("TODO").byFile();
                approve("b").byFile();
            }
        }
        """);
  }

  private void doHighlightTest(String code) {
    myFixture.configureByText("Test.java", code);
    myFixture.checkHighlighting();
  }
}
