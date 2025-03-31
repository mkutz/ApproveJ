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
import java.util.regex.Matcher;
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

  /**
   * Creates a new {@link FileVerifier} that uses the given {@link PathProvider} to determine the
   * paths of approved and received files.
   *
   * @param pathProvider the provider for the paths of the approved and received files
   */
  public FileVerifier(PathProvider pathProvider) {
    this.pathProvider = pathProvider;
  }

  /**
   * Creates a new {@link FileVerifier} that uses the stack trace to determine the paths of approved
   * and received files.
   *
   * @see StackTracePathProvider
   */
  public FileVerifier() {
    this(new StackTracePathProvider());
  }

  /**
   * Creates a new {@link FileVerifier} that uses the stack trace to determine the paths of approved
   * and received files.
   *
   * @see StackTracePathProvider
   */
  public FileVerifier(String filenameExtension) {
    this(new StackTracePathProvider(filenameExtension));
  }

  @Override
  public void accept(String received) {
    try {
      if (!exists(pathProvider.approvedPath())) {
        createFile(pathProvider.approvedPath());
      }
      String previouslyApproved = readString(pathProvider.approvedPath()).trim();
      if (!previouslyApproved.equals(received)) {
        writeString(pathProvider.receivedPath(), received, CREATE, TRUNCATE_EXISTING);
        throw new ApprovalError(received, previouslyApproved);
      }
      deleteIfExists(pathProvider.receivedPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** A provider for the paths of the approved and received files. */
  public interface PathProvider {

    /**
     * @return the path to the approved file
     */
    Path approvedPath();

    /**
     * @return the path to the received file
     */
    Path receivedPath();
  }

  static final class BasePathProvider implements PathProvider {

    private static final Pattern FILE_NAME_PATTERN =
        Pattern.compile("(?<baseName>.*)\\.(?<extension>.*)$");
    private final Path receivedPath;
    private final Path approvedPath;

    BasePathProvider(Path basePath) {
      Path parentPath = basePath.getParent();
      Matcher matcher = FILE_NAME_PATTERN.matcher(basePath.getFileName().toString());
      String baseName =
          matcher.matches() ? matcher.group("baseName") : basePath.getFileName().toString();
      String extension = matcher.matches() ? matcher.group("extension") : "txt";
      receivedPath = parentPath.resolve("%s-received.%s".formatted(baseName, extension));
      approvedPath = parentPath.resolve("%s-approved.%s".formatted(baseName, extension));
    }

    @Override
    public Path receivedPath() {
      return receivedPath;
    }

    @Override
    public Path approvedPath() {
      return approvedPath;
    }
  }

  /**
   * A {@link PathProvider} that uses a stack trace to determine the paths of the approved and
   * received files.
   *
   * @param stackTrace the stack trace of the current thread.
   */
  record StackTracePathProvider(StackTraceElement[] stackTrace, String filenameExtension)
      implements PathProvider {

    /**
     * Creates a new {@link StackTracePathProvider} that uses the stack trace of the current thread.
     *
     * @see Thread#currentThread()
     * @see Thread#getStackTrace()
     */
    StackTracePathProvider() {
      this(Thread.currentThread().getStackTrace(), "txt");
    }

    /**
     * Creates a new {@link StackTracePathProvider} that uses the stack trace of the current thread.
     *
     * @see Thread#currentThread()
     * @see Thread#getStackTrace()
     */
    StackTracePathProvider(String filenameExtension) {
      this(Thread.currentThread().getStackTrace(), filenameExtension);
    }

    @Override
    public Path approvedPath() {
      Method method = currentTestMethod();
      return Path.of(
          "src/test/java/%s/%s_%s_approved.%s"
              .formatted(
                  method.getDeclaringClass().getPackageName().replace(".", "/"),
                  method.getDeclaringClass().getSimpleName(),
                  method.getName(),
                  filenameExtension));
    }

    @Override
    public Path receivedPath() {
      Method method = currentTestMethod();
      return Path.of(
          "src/test/java/%s/%s-%s_received.%s"
              .formatted(
                  method.getDeclaringClass().getPackageName().replace(".", "/"),
                  method.getDeclaringClass().getSimpleName(),
                  method.getName(),
                  filenameExtension));
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
