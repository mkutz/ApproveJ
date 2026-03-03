package org.approvej.intellij;

import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

public class ApproveCallLineMarkerProviderTest extends LightJavaCodeInsightFixtureTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
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

  public void testMarker_absent_for_byValue() {
    myFixture.configureByText(
        "Test.java",
        """
        import static org.approvej.ApprovalBuilder.approve;
        class Test {
            void test() {
                approve("hello").byValue("expected");
            }
        }
        """);
    myFixture.doHighlighting();

    var markers =
        DaemonCodeAnalyzerImpl.getLineMarkers(myFixture.getEditor().getDocument(), getProject());
    assertTrue(
        "No line markers expected for byValue() chain",
        markers.stream()
            .noneMatch(
                m -> {
                  String tooltip = m.getLineMarkerTooltip();
                  return tooltip != null && tooltip.contains("approved");
                }));
  }

  public void testMarker_absent_for_unrelated_approve() {
    myFixture.configureByText(
        "Test.java",
        """
        class Test {
            void approve(String s) {}
            void test() {
                approve("hello");
            }
        }
        """);
    myFixture.doHighlighting();

    var markers =
        DaemonCodeAnalyzerImpl.getLineMarkers(myFixture.getEditor().getDocument(), getProject());
    assertTrue(
        "No line markers expected for unrelated approve() method",
        markers.stream()
            .noneMatch(
                m -> {
                  String tooltip = m.getLineMarkerTooltip();
                  return tooltip != null && tooltip.contains("approved");
                }));
  }
}
