package org.approvej.intellij;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

public class ApproveJRenamePsiElementProcessorPlatformTest
    extends LightJavaCodeInsightFixtureTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.addClass(
        """
        package org.approvej;
        public class ApprovalBuilder<T> {
            public static <T> ApprovalBuilder<T> approve(T value) { return null; }
            public void byFile() {}
        }
        """);
  }

  public void testRenameMethod() {
    PsiClass testClass = addTestClassWithMethod("com.example", "MyTest", "byValue");
    addInventory("src/com/example/MyTest-byValue-approved.txt", "com.example.MyTest#byValue");
    myFixture.addFileToProject("src/com/example/MyTest-byValue-approved.txt", "approved content");

    renameMethod(testClass, "byValue", "byContent");

    assertNotNull(myFixture.findFileInTempDir("src/com/example/MyTest-byContent-approved.txt"));
    assertNull(myFixture.findFileInTempDir("src/com/example/MyTest-byValue-approved.txt"));
  }

  public void testRenameMethod_with_received_file() {
    PsiClass testClass = addTestClassWithMethod("com.example", "MyTest", "byValue");
    addInventory("src/com/example/MyTest-byValue-approved.txt", "com.example.MyTest#byValue");
    myFixture.addFileToProject("src/com/example/MyTest-byValue-approved.txt", "approved");
    myFixture.addFileToProject("src/com/example/MyTest-byValue-received.txt", "received");

    renameMethod(testClass, "byValue", "byContent");

    assertNotNull(myFixture.findFileInTempDir("src/com/example/MyTest-byContent-approved.txt"));
    assertNotNull(myFixture.findFileInTempDir("src/com/example/MyTest-byContent-received.txt"));
    assertNull(myFixture.findFileInTempDir("src/com/example/MyTest-byValue-approved.txt"));
    assertNull(myFixture.findFileInTempDir("src/com/example/MyTest-byValue-received.txt"));
  }

  public void testRenameMethod_with_affix() {
    PsiClass testClass = addTestClassWithMethod("com.example", "MyTest", "approve_named");
    addInventory(
        "src/com/example/MyTest-approve_named-jane-approved.txt",
        "com.example.MyTest#approve_named");
    myFixture.addFileToProject(
        "src/com/example/MyTest-approve_named-jane-approved.txt", "approved");

    renameMethod(testClass, "approve_named", "approve_person");

    assertNotNull(
        myFixture.findFileInTempDir("src/com/example/MyTest-approve_person-jane-approved.txt"));
    assertNull(
        myFixture.findFileInTempDir("src/com/example/MyTest-approve_named-jane-approved.txt"));
  }

  public void testRenameMethod_multiple_files() {
    PsiClass testClass = addTestClassWithMethod("com.example", "MyTest", "approve_named");
    addInventory(
        "src/com/example/MyTest-approve_named-jane-approved.txt",
        "com.example.MyTest#approve_named",
        "src/com/example/MyTest-approve_named-john-approved.txt",
        "com.example.MyTest#approve_named");
    myFixture.addFileToProject("src/com/example/MyTest-approve_named-jane-approved.txt", "jane");
    myFixture.addFileToProject("src/com/example/MyTest-approve_named-john-approved.txt", "john");

    renameMethod(testClass, "approve_named", "approve_person");

    assertNotNull(
        myFixture.findFileInTempDir("src/com/example/MyTest-approve_person-jane-approved.txt"));
    assertNotNull(
        myFixture.findFileInTempDir("src/com/example/MyTest-approve_person-john-approved.txt"));
  }

  public void testRenameMethod_subdirectory_pattern() {
    PsiClass testClass = addTestClassWithMethod("com.example", "MyTest", "subdir_test");
    addInventory(
        "src/com/example/MyTest/subdir_test-approved.txt", "com.example.MyTest#subdir_test");
    myFixture.addFileToProject("src/com/example/MyTest/subdir_test-approved.txt", "approved");

    renameMethod(testClass, "subdir_test", "renamed_test");

    assertNotNull(myFixture.findFileInTempDir("src/com/example/MyTest/renamed_test-approved.txt"));
    assertNull(myFixture.findFileInTempDir("src/com/example/MyTest/subdir_test-approved.txt"));
  }

  public void testRenameClass() {
    PsiClass testClass = addTestClassWithMethod("com.example", "OldTest", "myMethod");
    addInventory("src/com/example/OldTest-myMethod-approved.txt", "com.example.OldTest#myMethod");
    myFixture.addFileToProject("src/com/example/OldTest-myMethod-approved.txt", "approved");

    myFixture.renameElement(testClass, "NewTest");

    assertNotNull(myFixture.findFileInTempDir("src/com/example/NewTest-myMethod-approved.txt"));
    assertNull(myFixture.findFileInTempDir("src/com/example/OldTest-myMethod-approved.txt"));
  }

  public void testRenameClass_subdirectory() {
    PsiClass testClass = addTestClassWithMethod("com.example", "OldTest", "myMethod");
    addInventory("src/com/example/OldTest/myMethod-approved.txt", "com.example.OldTest#myMethod");
    myFixture.addFileToProject("src/com/example/OldTest/myMethod-approved.txt", "approved");

    myFixture.renameElement(testClass, "NewTest");

    assertNotNull(myFixture.findFileInTempDir("src/com/example/NewTest/myMethod-approved.txt"));
    assertNull(myFixture.findFileInTempDir("src/com/example/OldTest/myMethod-approved.txt"));
  }

  public void testRenameMethod_no_inventory() {
    PsiClass testClass = addTestClassWithMethod("com.example", "MyTest", "byValue");

    renameMethod(testClass, "byValue", "byContent");

    PsiMethod[] methods = testClass.findMethodsByName("byContent", false);
    assertEquals(1, methods.length);
  }

  public void testRenameMethod_no_approved_files() {
    PsiClass testClass = addTestClassWithMethod("com.example", "MyTest", "byValue");
    addInventory("src/com/example/Other-otherMethod-approved.txt", "com.example.Other#otherMethod");

    renameMethod(testClass, "byValue", "byContent");

    PsiMethod[] methods = testClass.findMethodsByName("byContent", false);
    assertEquals(1, methods.length);
  }

  private PsiClass addTestClassWithMethod(String packageName, String className, String methodName) {
    return myFixture.addClass(
        """
        package %s;
        import static org.approvej.ApprovalBuilder.approve;
        public class %s {
            void %s() {
                approve("hello").byFile();
            }
        }
        """
            .formatted(packageName, className, methodName));
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

  private void renameMethod(PsiClass psiClass, String oldName, String newName) {
    PsiMethod[] methods = psiClass.findMethodsByName(oldName, false);
    assertEquals("Expected exactly one method named " + oldName, 1, methods.length);
    myFixture.renameElement(methods[0], newName);
  }
}
