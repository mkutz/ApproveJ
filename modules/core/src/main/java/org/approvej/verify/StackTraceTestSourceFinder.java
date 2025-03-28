package org.approvej.verify;

import static java.util.Arrays.stream;

import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class StackTraceTestSourceFinder {

  public static Optional<Path> currentTestSourceFile() {
    return stream(Thread.currentThread().getStackTrace())
        .map(
            element -> {
              try {
                return Class.forName(element.getClassName());
              } catch (ClassNotFoundException e) {
                return Void.class;
              }
            })
        .filter(
            elementClass ->
                stream(elementClass.getDeclaredMethods())
                    .anyMatch(method -> method.isAnnotationPresent(Test.class)))
        .map(
            clazz -> {
              var classLocation = clazz.getName().replace('.', '/') + ".java";
              return Path.of("src/test/java", classLocation).normalize();
            })
        .filter(path -> path.toFile().exists())
        .findFirst();
  }
}
