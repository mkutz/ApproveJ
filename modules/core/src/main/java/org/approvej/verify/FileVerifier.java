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
   * Creates a new {@link Verifier} that uses the given {@link PathProvider} to determine the paths
   * of approved and received files.
   *
   * @param pathProvider the provider for the paths of the approved and received files
   * @return a new {@link FileVerifier} that uses the given {@link PathProvider}
   */
  public static FileVerifier file(PathProvider pathProvider) {
    return new FileVerifier(pathProvider);
  }

  /**
   * Creates a new {@link Verifier} that uses the stack trace to determine the paths of approved and
   * received files.
   *
   * @return a new {@link FileVerifier} that uses a {@link StackTracePathProvider} to determine the
   *     paths
   */
  public static FileVerifier file() {
    return new FileVerifier(new StackTracePathProvider());
  }

  /**
   * Creates a new {@link Verifier} that uses the stack trace to determine the paths of approved and
   * received files.
   *
   * @param filenameExtension the file extension to use for the approved and received files
   * @return a new {@link FileVerifier} that uses a {@link StackTracePathProvider} to determine the
   *     paths
   * @see StackTracePathProvider
   */
  public static FileVerifier file(String filenameExtension) {
    return new FileVerifier(new StackTracePathProvider(filenameExtension));
  }

  private FileVerifier(PathProvider pathProvider) {
    this.pathProvider = pathProvider;
  }

  @Override
  public void accept(String received) {
    String trimmed = received.trim();
    try {
      if (!exists(pathProvider.approvedPath())) {
        createFile(pathProvider.approvedPath());
      }
      String previouslyApproved = readString(pathProvider.approvedPath()).trim();
      if (!previouslyApproved.equals(trimmed)) {
        writeString(pathProvider.receivedPath(), trimmed, CREATE, TRUNCATE_EXISTING);
        throw new ApprovalError(trimmed, previouslyApproved);
      }
      deleteIfExists(pathProvider.receivedPath());
    } catch (IOException e) {
      throw new FileVerifierError(e);
    }
  }

  /** A provider for the paths of the approved and received files. */
  public interface PathProvider {

    /**
     * The infix of the file containing the latest received value that didn't match the previously
     * approved.
     */
    String RECEIVED = "received";

    /** The infix of the file containing a previously approved value. */
    String APPROVED = "approved";

    /**
     * The path of the file containing the latest received value that didn't match the previously
     * approved.
     *
     * @return the {@link Path} to the received file
     */
    Path receivedPath();

    /**
     * The path of the file containing a previously approved value.
     *
     * @return the {@link Path} to the approved file
     */
    Path approvedPath();
  }

  /**
   * A {@link PathProvider} that uses the path of a previously approved file to determine the path
   * the received file and the filename extension.
   *
   * <p>For example,
   *
   * <ol>
   *   <li>if the given approved path is {@code /path/to/file-approved.json}, the {@link
   *       #receivedPath} will be {@code /path/to/file-received.json}
   *   <li>if the given approved path is {@code /path/to/file.txt}, the {@link #receivedPath} will
   *       be {@code /path/to/file-received.txt}
   * </ol>
   *
   * <p>Note that the {@code approved} infix is not enforced on the given approved file path. It is
   * also not necessary for the approved file to exist.
   */
  static final class BasePathProvider implements PathProvider {

    private static final Pattern FILE_NAME_PATTERN =
        Pattern.compile("(?<baseName>.*?)(?<approved>-" + APPROVED + ")?\\.(?<extension>.*)$");
    private final Path receivedPath;
    private final Path approvedPath;

    public static BasePathProvider approvedPath(Path approvedPath) {
      return new BasePathProvider(approvedPath);
    }

    private BasePathProvider(Path approvedPath) {
      this.approvedPath = approvedPath;
      Path parentPath = approvedPath.getParent();
      Matcher matcher = FILE_NAME_PATTERN.matcher(approvedPath.getFileName().toString());
      String baseName =
          matcher.matches() ? matcher.group("baseName") : approvedPath.getFileName().toString();
      String extension = matcher.matches() ? matcher.group("extension") : "txt";
      this.receivedPath = parentPath.resolve("%s-%s.%s".formatted(baseName, RECEIVED, extension));
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
   */
  static final class StackTracePathProvider implements PathProvider {

    private final Path receivedPath;
    private final Path approvedPath;

    /**
     * Creates a new {@link StackTracePathProvider} that uses the stack trace of the current thread.
     *
     * @see Thread#currentThread()
     * @see Thread#getStackTrace()
     */
    StackTracePathProvider(String filenameExtension) {
      Method method = currentTestMethod();
      Path basePath =
          Path.of(
              "src/test/java/%s"
                  .formatted(method.getDeclaringClass().getPackageName().replace(".", "/")));
      String fileNamePattern =
          "%s-%s-%%s.%s"
              .formatted(
                  method.getDeclaringClass().getSimpleName(), method.getName(), filenameExtension);
      receivedPath = basePath.resolve(fileNamePattern.formatted(RECEIVED));
      approvedPath = basePath.resolve(fileNamePattern.formatted(APPROVED));
    }

    /**
     * Creates a new {@link StackTracePathProvider} that uses the stack trace of the current thread.
     *
     * @see Thread#currentThread()
     * @see Thread#getStackTrace()
     */
    StackTracePathProvider() {
      this("txt");
    }

    @Override
    public Path approvedPath() {
      return approvedPath;
    }

    @Override
    public Path receivedPath() {
      return receivedPath;
    }

    private Method currentTestMethod() {
      return stream(Thread.currentThread().getStackTrace())
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

  static class FileVerifierError extends RuntimeException {
    public FileVerifierError(Throwable cause) {
      super("Failed to verify file", cause);
    }
  }
}
