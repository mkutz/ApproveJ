package org.approvej.approve;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.notExists;
import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.IOException;
import java.nio.file.Path;
import org.approvej.ApprovalError;
import org.jspecify.annotations.NullMarked;

/**
 * {@link Approver} that compares the received value with the approved value stored in a file. If
 * the values differ, it creates a new file with the received value and throws an {@link
 * ApprovalError}.
 */
@NullMarked
public class FileApprover implements Approver {

  private final PathProvider pathProvider;

  /**
   * Creates a new {@link FileApprover} that uses the given {@link PathProvider} to determine the
   * paths of the approved and received files.
   *
   * @param pathProvider a {@link PathProvider} to determine the paths of the approved and received
   *     files
   */
  FileApprover(PathProvider pathProvider) {
    this.pathProvider = pathProvider;
  }

  @Override
  public ApprovalResult apply(String received) {
    ensureDirectory();
    ensureApprovedFile();
    return check(readApprovedFile(), received.trim());
  }

  private void ensureDirectory() {
    String description = "create directories %s".formatted(pathProvider.directory());
    try {
      createDirectories(pathProvider.directory());
    } catch (IOException e) {
      throw new FileApproverError("Failed to %s".formatted(description), e);
    }
  }

  private void ensureApprovedFile() {
    Path approvedPath = pathProvider.approvedPath();
    if (notExists(approvedPath)) {
      try {
        createFile(approvedPath);
      } catch (IOException e) {
        throw new FileApproverError(
            "Failed to %s".formatted("create approved file %s".formatted(approvedPath)), e);
      }
    }
  }

  private String readApprovedFile() {
    Path approvedPath = pathProvider.approvedPath();
    try {
      return readString(approvedPath).trim();
    } catch (IOException e) {
      throw new FileApproverError(
          "Failed to %s".formatted("read approved file %s".formatted(approvedPath)), e);
    }
  }

  private ApprovalResult check(String previouslyApproved, String receivedTrimmed) {
    ApprovalResult result =
        new FileApprovalResult(previouslyApproved, receivedTrimmed, pathProvider);
    Path receivedPath = pathProvider.receivedPath();
    if (result.needsApproval()) {
      try {
        writeString(receivedPath, receivedTrimmed, CREATE, TRUNCATE_EXISTING);
      } catch (IOException e) {
        throw new FileApproverError(
            "Failed to %s".formatted("write received to %s".formatted(receivedPath)), e);
      }
      return result;
    }
    try {
      deleteIfExists(receivedPath);
      return result;
    } catch (IOException e) {
      throw new FileApproverError(
          "Failed to %s".formatted("delete received file %s".formatted(receivedPath)), e);
    }
  }
}
