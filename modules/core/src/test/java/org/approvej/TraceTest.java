package org.approvej;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TraceTest {

  @Test
  void testing() {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

    for (StackTraceElement element : stackTrace) {
      try {
        Class<?> elementClass = Class.forName(element.getClassName());
        Optional<Method> testMethod =
            Arrays.stream(elementClass.getDeclaredMethods())
                .filter(method -> method.getDeclaredAnnotation(Test.class) != null)
                .findFirst();
        if (testMethod.isPresent()) {
          File sourceFile = getSourceFile(elementClass);
          if (sourceFile != null) {
            System.out.println("Test method: " + testMethod.get().getName());
            System.out.println("Source file: " + sourceFile);
          }
        }
      } catch (ClassNotFoundException e) {
        // Handle exception
      }
    }
  }

  public static File getSourceFile(Class<?> clazz) {
    CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
    if (codeSource != null) {
      URL location = codeSource.getLocation();
      String className = clazz.getName().replace('.', '/') + ".java";
      File sourceFile =
          Paths.get(location.getPath(), "../../../../src/test/java", className)
              .normalize()
              .toFile();
      if (sourceFile.exists()) {
        return sourceFile;
      }
    }
    return null;
  }
}
