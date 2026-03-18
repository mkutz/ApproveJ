package org.approvej.intellij

import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

class DuplicateUnnamedApprovalInspectionTest : LightJavaCodeInsightFixtureTestCase() {

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(DuplicateUnnamedApprovalInspection())
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

  fun testTwo_unnamed_approveImage_calls() {
    doHighlightTest(
      """
      import static org.approvej.image.ImageApprovalBuilder.approveImage;
      class Test {
          void test() {
              <warning descr="$UNNAMED_WARNING">approveImage(null).byFile()</warning>;
              <warning descr="$UNNAMED_WARNING">approveImage(null).byFile()</warning>;
          }
      }
      """
        .trimIndent()
    )
  }

  fun testTwo_unnamed_byFile_calls() {
    doHighlightTest(
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              <warning descr="$UNNAMED_WARNING">approve("a").byFile()</warning>;
              <warning descr="$UNNAMED_WARNING">approve("b").byFile()</warning>;
          }
      }
      """
        .trimIndent()
    )
  }

  fun testThree_unnamed_byFile_calls() {
    doHighlightTest(
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              <warning descr="$UNNAMED_WARNING">approve("a").byFile()</warning>;
              <warning descr="$UNNAMED_WARNING">approve("b").byFile()</warning>;
              <warning descr="$UNNAMED_WARNING">approve("c").byFile()</warning>;
          }
      }
      """
        .trimIndent()
    )
  }

  fun testSingle_unnamed_byFile_call() {
    doHighlightTest(
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              approve("a").byFile();
          }
      }
      """
        .trimIndent()
    )
  }

  fun testTwo_byFile_calls_with_different_names() {
    doHighlightTest(
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              approve("a").named("first").byFile();
              approve("b").named("second").byFile();
          }
      }
      """
        .trimIndent()
    )
  }

  fun testTwo_byFile_calls_one_named_one_unnamed() {
    doHighlightTest(
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              approve("a").named("first").byFile();
              approve("b").byFile();
          }
      }
      """
        .trimIndent()
    )
  }

  fun testTwo_byFile_calls_with_same_name() {
    val warning =
      "Duplicate approval name 'foo': each subsequent byFile() overwrites the previous" +
        " approved file. Use distinct names."
    doHighlightTest(
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              <warning descr="$warning">approve("a").named("foo").byFile()</warning>;
              <warning descr="$warning">approve("b").named("foo").byFile()</warning>;
          }
      }
      """
        .trimIndent()
    )
  }

  fun testNamed_with_non_constant_arg_excluded() {
    myFixture.addClass(
      """
      package org.example;
      public class Names {
          public static String get() { return "dynamic"; }
      }
      """
        .trimIndent()
    )
    doHighlightTest(
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              approve("a").named(org.example.Names.get()).byFile();
              approve("b").byFile();
          }
      }
      """
        .trimIndent()
    )
  }

  fun testByFile_with_args_excluded() {
    doHighlightTest(
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              approve("a").byFile("/some/path");
              approve("b").byFile("/other/path");
          }
      }
      """
        .trimIndent()
    )
  }

  fun testTwo_byValue_calls() {
    doHighlightTest(
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              approve("a").byValue("expected1");
              approve("b").byValue("expected2");
          }
      }
      """
        .trimIndent()
    )
  }

  fun testMixed_byFile_and_byValue() {
    doHighlightTest(
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              approve("a").byFile();
              approve("b").byValue("expected");
          }
      }
      """
        .trimIndent()
    )
  }

  fun testQuickfix_add_named_to_single() {
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
      """
        .trimIndent(),
    )
    val fix = myFixture.findSingleIntention("Add .named(\"TODO\")")
    myFixture.launchAction(fix)
    myFixture.checkResult(
      """
      import static org.approvej.ApprovalBuilder.approve;
      class Test {
          void test() {
              approve("a").named("TODO").byFile();
              approve("b").byFile();
          }
      }
      """
        .trimIndent()
    )
  }

  private fun doHighlightTest(code: String) {
    myFixture.configureByText("Test.java", code)
    myFixture.checkHighlighting()
  }

  companion object {
    private const val UNNAMED_WARNING =
      "Duplicate unnamed approval: each subsequent byFile() overwrites the previous approved file." +
        " Use .named() to distinguish."
  }
}
