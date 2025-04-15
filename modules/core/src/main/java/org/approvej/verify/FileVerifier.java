package org.approvej.verify;

import static java.nio.file.Files.createFile;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.approvej.verify.NextToTestPathProvider.nextToTest;
import static org.approvej.verify.NextToTestPathProvider.nextToTestAs;

import java.io.IOException;
import org.approvej.ApprovalError;

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
  public static FileVerifier inFile(PathProvider pathProvider) {
    return new FileVerifier(pathProvider);
  }

  /**
   * Creates a new {@link Verifier} that uses the stack trace to determine the paths of approved and
   * received files.
   *
   * @return a new {@link FileVerifier} that uses a {@link NextToTestPathProvider} to determine the
   *     paths
   */
  public static FileVerifier inFile() {
    return new FileVerifier(nextToTest());
  }

  /**
   * Creates a new {@link Verifier} that uses the stack trace to determine the paths of approved and
   * received files.
   *
   * @param filenameExtension the file extension to use for the approved and received files
   * @return a new {@link FileVerifier} that uses a {@link NextToTestPathProvider} to determine the
   *     paths
   * @see NextToTestPathProvider
   */
  public static FileVerifier inFile(String filenameExtension) {
    return new FileVerifier(nextToTestAs(filenameExtension));
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
}
