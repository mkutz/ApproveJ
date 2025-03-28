package org.approvej.verify;

import static java.nio.file.Files.createFile;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.Arrays.stream;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.regex.Pattern;
import org.approvej.ApprovalError;
import org.junit.jupiter.api.Test;

/**
 * {@link Verifier} that compares the received value with the approved value stored in a file. If
 * the values differ, it creates a new file with the received value and throws an {@link
 * ApprovalError}.
 */
public class FileVerifier implements Verifier {

  private static final Pattern FILE_NAME_PATTERN = Pattern.compile("(.*)\\.(.*)$");

  final Path receivedPath;
  final Path approvedPath;

  public FileVerifier(Path filePath) {
    String fileName = filePath.getFileName().toString();
    String approvedFileName = FILE_NAME_PATTERN.matcher(fileName).replaceFirst("$1_approved.txt");
    String receivedFileName = FILE_NAME_PATTERN.matcher(fileName).replaceFirst("$1_received.txt");
    this.receivedPath = filePath.getParent().resolve(receivedFileName);
    this.approvedPath = filePath.getParent().resolve(approvedFileName);
  }

  /** Creates a {@link FileVerifier} that will store the received/approved files next to the test */
  public FileVerifier() {
    this(currentTestSourceFile().orElse(Path.of("src/test/resources/approvej/test.txt")));
  }

  static Optional<Path> currentTestSourceFile() {
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

  @Override
  public void accept(String received) {
    try {
      if (!exists(approvedPath)) {
        createFile(approvedPath);
      }
      String previouslyApproved = readString(approvedPath);
      if (!previouslyApproved.equals(received)) {
        writeString(receivedPath, received, CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        throw new ApprovalError(received, previouslyApproved);
      }
      deleteIfExists(receivedPath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
