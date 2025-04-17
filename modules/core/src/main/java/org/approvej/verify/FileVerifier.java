package org.approvej.verify;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.notExists;
import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.IOException;
import org.approvej.ApprovalError;
import org.jspecify.annotations.NullMarked;

/**
 * {@link Verifier} that compares the received value with the approved value stored in a file. If
 * the values differ, it creates a new file with the received value and throws an {@link
 * ApprovalError}.
 */
@NullMarked
public class FileVerifier implements Verifier {

  private final PathProvider pathProvider;

  /**
   * Creates a new {@link FileVerifier} that uses the given {@link PathProvider} to determine the
   * paths of the approved and received files.
   *
   * @param pathProvider a {@link PathProvider} to determine the paths of the approved and received
   *     files
   */
  FileVerifier(PathProvider pathProvider) {
    this.pathProvider = pathProvider;
  }

  @Override
  public void accept(String received) {
    String trimmed = received.trim();
    try {
      createDirectories(pathProvider.directory());
      if (notExists(pathProvider.approvedPath())) {
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
