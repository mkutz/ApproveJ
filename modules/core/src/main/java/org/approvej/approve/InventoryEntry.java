package org.approvej.approve;

import java.nio.file.Path;
import org.jspecify.annotations.NullMarked;

/** An entry in the approved file inventory, mapping a file path to its originating test method. */
@NullMarked
record InventoryEntry(Path relativePath, String className, String methodName) {

  InventoryEntry(Path relativePath, String testReference) {
    this(relativePath, parseClassName(testReference), parseMethodName(testReference));
  }

  String testReference() {
    return "%s#%s".formatted(className, methodName);
  }

  private static String parseClassName(String testReference) {
    int hashIndex = testReference.indexOf('#');
    if (hashIndex < 0) {
      throw new IllegalArgumentException(
          "Invalid test reference (expected 'className#methodName'): %s".formatted(testReference));
    }
    return testReference.substring(0, hashIndex);
  }

  private static String parseMethodName(String testReference) {
    return testReference.substring(testReference.indexOf('#') + 1);
  }
}
