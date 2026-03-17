package org.approvej.intellij

import com.intellij.psi.PsiClass
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

class ApproveJRenamePsiElementProcessorPlatformTest : LightJavaCodeInsightFixtureTestCase() {

  override fun setUp() {
    super.setUp()
    myFixture.addClass(
      """
      package org.approvej;
      public class ApprovalBuilder<T> {
          public static <T> ApprovalBuilder<T> approve(T value) { return null; }
          public void byFile() {}
      }
      """
        .trimIndent()
    )
  }

  fun testRenameMethod() {
    val testClass = addTestClassWithMethod("com.example", "MyTest", "byValue")
    addInventory("src/com/example/MyTest-byValue-approved.txt", "com.example.MyTest#byValue")
    myFixture.addFileToProject("src/com/example/MyTest-byValue-approved.txt", "approved content")

    renameMethod(testClass, "byValue", "byContent")

    assertNotNull(myFixture.findFileInTempDir("src/com/example/MyTest-byContent-approved.txt"))
    assertNull(myFixture.findFileInTempDir("src/com/example/MyTest-byValue-approved.txt"))
  }

  fun testRenameMethod_with_received_file() {
    val testClass = addTestClassWithMethod("com.example", "MyTest", "byValue")
    addInventory("src/com/example/MyTest-byValue-approved.txt", "com.example.MyTest#byValue")
    myFixture.addFileToProject("src/com/example/MyTest-byValue-approved.txt", "approved")
    myFixture.addFileToProject("src/com/example/MyTest-byValue-received.txt", "received")

    renameMethod(testClass, "byValue", "byContent")

    assertNotNull(myFixture.findFileInTempDir("src/com/example/MyTest-byContent-approved.txt"))
    assertNotNull(myFixture.findFileInTempDir("src/com/example/MyTest-byContent-received.txt"))
    assertNull(myFixture.findFileInTempDir("src/com/example/MyTest-byValue-approved.txt"))
    assertNull(myFixture.findFileInTempDir("src/com/example/MyTest-byValue-received.txt"))
  }

  fun testRenameMethod_with_affix() {
    val testClass = addTestClassWithMethod("com.example", "MyTest", "approve_named")
    addInventory(
      "src/com/example/MyTest-approve_named-jane-approved.txt",
      "com.example.MyTest#approve_named",
    )
    myFixture.addFileToProject("src/com/example/MyTest-approve_named-jane-approved.txt", "approved")

    renameMethod(testClass, "approve_named", "approve_person")

    assertNotNull(
      myFixture.findFileInTempDir("src/com/example/MyTest-approve_person-jane-approved.txt")
    )
    assertNull(
      myFixture.findFileInTempDir("src/com/example/MyTest-approve_named-jane-approved.txt")
    )
  }

  fun testRenameMethod_multiple_files() {
    val testClass = addTestClassWithMethod("com.example", "MyTest", "approve_named")
    addInventory(
      "src/com/example/MyTest-approve_named-jane-approved.txt",
      "com.example.MyTest#approve_named",
      "src/com/example/MyTest-approve_named-john-approved.txt",
      "com.example.MyTest#approve_named",
    )
    myFixture.addFileToProject("src/com/example/MyTest-approve_named-jane-approved.txt", "jane")
    myFixture.addFileToProject("src/com/example/MyTest-approve_named-john-approved.txt", "john")

    renameMethod(testClass, "approve_named", "approve_person")

    assertNotNull(
      myFixture.findFileInTempDir("src/com/example/MyTest-approve_person-jane-approved.txt")
    )
    assertNotNull(
      myFixture.findFileInTempDir("src/com/example/MyTest-approve_person-john-approved.txt")
    )
  }

  fun testRenameMethod_subdirectory_pattern() {
    val testClass = addTestClassWithMethod("com.example", "MyTest", "subdir_test")
    addInventory(
      "src/com/example/MyTest/subdir_test-approved.txt",
      "com.example.MyTest#subdir_test",
    )
    myFixture.addFileToProject("src/com/example/MyTest/subdir_test-approved.txt", "approved")

    renameMethod(testClass, "subdir_test", "renamed_test")

    assertNotNull(myFixture.findFileInTempDir("src/com/example/MyTest/renamed_test-approved.txt"))
    assertNull(myFixture.findFileInTempDir("src/com/example/MyTest/subdir_test-approved.txt"))
  }

  fun testRenameClass() {
    val testClass = addTestClassWithMethod("com.example", "OldTest", "myMethod")
    addInventory("src/com/example/OldTest-myMethod-approved.txt", "com.example.OldTest#myMethod")
    myFixture.addFileToProject("src/com/example/OldTest-myMethod-approved.txt", "approved")

    myFixture.renameElement(testClass, "NewTest")

    assertNotNull(myFixture.findFileInTempDir("src/com/example/NewTest-myMethod-approved.txt"))
    assertNull(myFixture.findFileInTempDir("src/com/example/OldTest-myMethod-approved.txt"))
  }

  fun testRenameClass_subdirectory() {
    val testClass = addTestClassWithMethod("com.example", "OldTest", "myMethod")
    addInventory("src/com/example/OldTest/myMethod-approved.txt", "com.example.OldTest#myMethod")
    myFixture.addFileToProject("src/com/example/OldTest/myMethod-approved.txt", "approved")

    myFixture.renameElement(testClass, "NewTest")

    assertNotNull(myFixture.findFileInTempDir("src/com/example/NewTest/myMethod-approved.txt"))
    assertNull(myFixture.findFileInTempDir("src/com/example/OldTest/myMethod-approved.txt"))
  }

  fun testRenameMethod_no_inventory() {
    val testClass = addTestClassWithMethod("com.example", "MyTest", "byValue")

    renameMethod(testClass, "byValue", "byContent")

    val methods = testClass.findMethodsByName("byContent", false)
    assertEquals(1, methods.size)
  }

  fun testRenameMethod_no_approved_files() {
    val testClass = addTestClassWithMethod("com.example", "MyTest", "byValue")
    addInventory("src/com/example/Other-otherMethod-approved.txt", "com.example.Other#otherMethod")

    renameMethod(testClass, "byValue", "byContent")

    val methods = testClass.findMethodsByName("byContent", false)
    assertEquals(1, methods.size)
  }

  private fun addTestClassWithMethod(
    packageName: String,
    className: String,
    methodName: String,
  ): PsiClass {
    return myFixture.addClass(
      """
      package $packageName;
      import static org.approvej.ApprovalBuilder.approve;
      public class $className {
          void $methodName() {
              approve("hello").byFile();
          }
      }
      """
        .trimIndent()
    )
  }

  private fun addInventory(vararg keysAndValues: String) {
    val sb = StringBuilder("# ApproveJ Approved File Inventory\n")
    var i = 0
    while (i < keysAndValues.size) {
      sb
        .append(keysAndValues[i].replace(" ", "\\ "))
        .append(" = ")
        .append(keysAndValues[i + 1])
        .append("\n")
      i += 2
    }
    myFixture.addFileToProject(".approvej/inventory.properties", sb.toString())
  }

  private fun renameMethod(psiClass: PsiClass, oldName: String, newName: String) {
    val methods = psiClass.findMethodsByName(oldName, false)
    assertEquals("Expected exactly one method named $oldName", 1, methods.size)
    myFixture.renameElement(methods[0], newName)
  }
}
