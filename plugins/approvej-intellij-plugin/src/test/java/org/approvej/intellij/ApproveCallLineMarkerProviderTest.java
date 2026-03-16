package org.approvej.intellij;

import com.intellij.codeInsight.daemon.GutterMark;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import java.util.List;

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
            public ApprovalBuilder<T> named(String name) { return this; }
            public void byFile() {}
            public void byValue(java.lang.String expected) {}
            public Object by(Object approver) { return null; }
        }
        """);
  }

  public void testMarker_absent_for_byValue() {
    addInventory("src/com/example/Test-test-approved.txt", "com.example.Test#test");
    myFixture.addFileToProject("src/com/example/Test-test-approved.txt", "approved content");

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
        """);

    List<GutterMark> gutters = findApprovalGutters();
    assertTrue("No gutter icons expected for byValue() chain", gutters.isEmpty());
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

    var gutters = myFixture.findAllGutters();
    assertTrue(
        "No gutter icons expected for unrelated approve() method",
        gutters.stream()
            .noneMatch(
                g -> {
                  String tooltip = g.getTooltipText();
                  return tooltip != null && tooltip.contains("approved");
                }));
  }

  public void testMarker_present_for_byFile() {
    addInventory("src/com/example/Test-myTest-approved.txt", "com.example.Test#myTest");
    myFixture.addFileToProject("src/com/example/Test-myTest-approved.txt", "approved content");

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
        """);

    List<GutterMark> gutters = findApprovalGutters();
    assertEquals(1, gutters.size());
    assertEquals("Navigate to Test-myTest-approved.txt", gutters.getFirst().getTooltipText());
  }

  public void testMarker_named_filters_to_matching_file() {
    addInventory(
        "src/com/example/Test-myTest-jane-approved.txt", "com.example.Test#myTest",
        "src/com/example/Test-myTest-john-approved.txt", "com.example.Test#myTest");
    myFixture.addFileToProject("src/com/example/Test-myTest-jane-approved.txt", "jane content");
    myFixture.addFileToProject("src/com/example/Test-myTest-john-approved.txt", "john content");

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
        """);

    List<GutterMark> gutters = findApprovalGutters();
    assertEquals(1, gutters.size());
    assertTrue(gutters.getFirst().getTooltipText().contains("jane"));
    assertFalse(gutters.getFirst().getTooltipText().contains("john"));
  }

  public void testMarker_unnamed_excludes_named_files() {
    addInventory(
        "src/com/example/Test-myTest-approved.txt", "com.example.Test#myTest",
        "src/com/example/Test-myTest-jane-approved.txt", "com.example.Test#myTest");
    myFixture.addFileToProject("src/com/example/Test-myTest-approved.txt", "unnamed content");
    myFixture.addFileToProject("src/com/example/Test-myTest-jane-approved.txt", "jane content");

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
        """);

    List<GutterMark> gutters = findApprovalGutters();
    assertEquals(1, gutters.size());
    assertEquals("Navigate to Test-myTest-approved.txt", gutters.getFirst().getTooltipText());
  }

  public void testMarker_multiple_named_approvals() {
    addInventory(
        "src/com/example/Test-myTest-jane-approved.txt", "com.example.Test#myTest",
        "src/com/example/Test-myTest-john-approved.txt", "com.example.Test#myTest");
    myFixture.addFileToProject("src/com/example/Test-myTest-jane-approved.txt", "jane content");
    myFixture.addFileToProject("src/com/example/Test-myTest-john-approved.txt", "john content");

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
        """);

    List<GutterMark> gutters = findApprovalGutters();
    assertEquals(2, gutters.size());
    assertFalse(
        "Expected distinct tooltips",
        gutters.get(0).getTooltipText().equals(gutters.get(1).getTooltipText()));
  }

  public void testMarker_named_approval_shown_when_sibling_unapproved() {
    addInventory(
        "src/com/example/Test-myTest-a-approved.txt", "com.example.Test#myTest",
        "src/com/example/Test-myTest-b-approved.txt", "com.example.Test#myTest");
    myFixture.addFileToProject("src/com/example/Test-myTest-a-approved.txt", "a content");
    myFixture.addFileToProject("src/com/example/Test-myTest-a-received.txt", "a different content");
    myFixture.addFileToProject("src/com/example/Test-myTest-b-approved.txt", "b content");

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
        """);

    List<GutterMark> gutters = findApprovalGutters();
    assertEquals(2, gutters.size());
    assertEquals("Received and approved files", gutters.getFirst().getTooltipText());
    assertEquals("Navigate to Test-myTest-b-approved.txt", gutters.get(1).getTooltipText());
  }

  public void testMarker_received_file_changes_icon() {
    addInventory("src/com/example/Test-myTest-approved.txt", "com.example.Test#myTest");
    myFixture.addFileToProject("src/com/example/Test-myTest-approved.txt", "approved content");
    myFixture.addFileToProject("src/com/example/Test-myTest-received.txt", "received content");

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
        """);

    List<GutterMark> gutters = findApprovalGutters();
    assertEquals(1, gutters.size());
    assertEquals("Received and approved files", gutters.getFirst().getTooltipText());
  }

  private List<GutterMark> findApprovalGutters() {
    return myFixture.findAllGutters().stream()
        .filter(
            g -> {
              String tooltip = g.getTooltipText();
              return tooltip != null
                  && (tooltip.contains("approved") || tooltip.contains("Received"));
            })
        .toList();
  }

  private void addInventory(String... keysAndValues) {
    var sb = new StringBuilder("# ApproveJ Approved File Inventory\n");
    for (int i = 0; i < keysAndValues.length; i += 2) {
      sb.append(keysAndValues[i].replace(" ", "\\ "))
          .append(" = ")
          .append(keysAndValues[i + 1])
          .append("\n");
    }
    myFixture.addFileToProject(".approvej/inventory.properties", sb.toString());
  }
}
