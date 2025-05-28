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
import java.util.concurrent.Callable;
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
  public void accept(String received) {
    String trimmed = received.trim();
    doIoOp(
        "create directories %s".formatted(pathProvider.directory()),
        () -> createDirectories(pathProvider.directory()));
    Path approvedPath = pathProvider.approvedPath();
    if (notExists(approvedPath)) {
      doIoOp("create approved file %s".formatted(approvedPath), () -> createFile(approvedPath));
    }
    String previouslyApproved =
        doIoOp(
            "read approved file %s".formatted(approvedPath), () -> readString(approvedPath).trim());
    Path receivedPath = pathProvider.receivedPath();
    if (!previouslyApproved.equals(trimmed)) {
      doIoOp(
          "write received to %s".formatted(receivedPath),
          () -> writeString(receivedPath, trimmed + "\n", CREATE, TRUNCATE_EXISTING));
      throw new ApprovalError(trimmed, previouslyApproved);
    }
    doIoOp("delete received file %s".formatted(receivedPath), () -> deleteIfExists(receivedPath));
  }

  private static <T> T doIoOp(String description, IoOp<T> op) {
    try {
      return op.call();
    } catch (IOException e) {
      throw new FileApproverError("Failed to %s".formatted(description), e);
    }
  }

  @FunctionalInterface
  private interface IoOp<T> extends Callable<T> {
    @Override
    T call() throws IOException;
  }
}
