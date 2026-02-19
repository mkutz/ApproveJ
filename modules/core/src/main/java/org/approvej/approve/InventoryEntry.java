package org.approvej.approve;

import org.jspecify.annotations.NullMarked;

/** An entry in the approved file inventory, mapping a file path to its originating test method. */
@NullMarked
record InventoryEntry(String relativePath, String className, String methodName) {

  InventoryEntry(String relativePath, String testReference) {
    this(relativePath, parseClassName(testReference), parseMethodName(testReference));
  }

  String testReference() {
    return className + "#" + methodName;
  }

  private static String parseClassName(String testReference) {
    int hashIndex = testReference.indexOf('#');
    if (hashIndex < 0) {
      throw new IllegalArgumentException(
          "Invalid test reference (expected 'className#methodName'): " + testReference);
    }
    return testReference.substring(0, hashIndex);
  }

  private static String parseMethodName(String testReference) {
    return testReference.substring(testReference.indexOf('#') + 1);
  }
}
