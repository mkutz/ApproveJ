package org.approvej.verify;

import static java.nio.file.Files.createFile;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.Arrays.stream;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.approvej.ApprovalError;
import org.junit.jupiter.api.Test;

/**
 * {@link Verifier} that compares the received value with the approved value stored in a file. If
 * the values differ, it creates a new file with the received value and throws an {@link
 * ApprovalError}.
 */
public class FileVerifier implements Verifier {

  private final PathProvider pathProvider;

  public FileVerifier(PathProvider pathProvider) {
    this.pathProvider = pathProvider;
  }

  public FileVerifier(Path basePath) {
    this(new StaticPathProvider(basePath));
  }

  public FileVerifier() {
    this(new StackTracePathProvider());
  }

  @Override
  public void accept(String received) {
    try {
      if (!exists(pathProvider.approvedPath())) {
        createFile(pathProvider.approvedPath());
      }
      String previouslyApproved = readString(pathProvider.approvedPath());
      if (!previouslyApproved.equals(received)) {
        writeString(pathProvider.receivedPath(), received, CREATE, TRUNCATE_EXISTING);
        throw new ApprovalError(received, previouslyApproved);
      }
      deleteIfExists(pathProvider.receivedPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public interface PathProvider {
    Path approvedPath();

    Path receivedPath();
  }

  record StaticPathProvider(Path receivedPath, Path approvedPath) implements PathProvider {

    private static final Pattern FILE_NAME_PATTERN = Pattern.compile("(.*)\\.(.*)$");

    StaticPathProvider(Path basePath) {
      this(
          basePath
              .getParent()
              .resolve(
                  FILE_NAME_PATTERN
                      .matcher(basePath.getFileName().toString())
                      .replaceFirst("$1_received.$2")),
          basePath
              .getParent()
              .resolve(
                  FILE_NAME_PATTERN
                      .matcher(basePath.getFileName().toString())
                      .replaceFirst("$1_approved.$2")));
    }
  }

  record StackTracePathProvider(StackTraceElement[] stackTrace) implements PathProvider {

    StackTracePathProvider() {
      this(Thread.currentThread().getStackTrace());
    }

    @Override
    public Path approvedPath() {
      Method method = currentTestMethod();
      return Path.of(
          "src/test/java/"
              + method.getDeclaringClass().getPackageName().replace(".", "/")
              + "/"
              + method.getDeclaringClass().getSimpleName()
              + "_"
              + method.getName()
              + "_approved.txt");
    }

    @Override
    public Path receivedPath() {
      Method method = currentTestMethod();
      return Path.of(
          "src/test/java/"
              + method.getDeclaringClass().getPackageName().replace(".", "/")
              + "/"
              + method.getDeclaringClass().getSimpleName()
              + "_"
              + method.getName()
              + "_received.txt");
    }

    private Method currentTestMethod() {
      return stream(stackTrace)
          .map(
              element -> {
                try {
                  return Class.forName(element.getClassName())
                      .getDeclaredMethod(element.getMethodName());
                } catch (ClassNotFoundException | NoSuchMethodException e) {
                  return null;
                }
              })
          .filter(method -> method != null && method.isAnnotationPresent(Test.class))
          .findFirst()
          .orElseThrow();
    }
  }
}
