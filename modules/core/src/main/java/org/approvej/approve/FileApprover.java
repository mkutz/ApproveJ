package org.approvej.approve;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.list;
import static java.nio.file.Files.move;
import static java.nio.file.Files.notExists;
import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.Comparator.comparing;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.approvej.ApprovalError;
import org.approvej.ApprovalResult;
import org.jspecify.annotations.NullMarked;

/**
 * {@link Approver} that compares the received value with the approved value stored in a file. If
 * the values differ, it creates a new file with the received value and throws an {@link
 * ApprovalError}.
 *
 * @param pathProvider a {@link PathProvider} to determine the paths of the approved and received
 *     files
 */
@NullMarked
record FileApprover(PathProvider pathProvider) implements Approver {

  @Override
  public ApprovalResult apply(String received) {
    ensureDirectory();
    handleOldApprovedFiles();
    ensureApprovedFile();
    return check(readApprovedFile(), received.trim());
  }

  private void ensureDirectory() {
    try {
      createDirectories(pathProvider.directory());
    } catch (IOException e) {
      throw new FileApproverError(
          "Creating directories %s failed".formatted(pathProvider.directory()), e);
    }
  }

  private void ensureApprovedFile() {
    Path approvedPath = pathProvider.approvedPath();
    if (notExists(approvedPath)) {
      try {
        createFile(approvedPath);
      } catch (IOException e) {
        throw new FileApproverError("Creating approved file %s failed".formatted(approvedPath), e);
      }
    }
  }

  private void handleOldApprovedFiles() {
    Path approvedPath = pathProvider.approvedPath();
    String filename = approvedPath.getFileName().toString();
    Pattern filenameExtensionPattern =
        Pattern.compile("(?<baseFilename>.+?)(?:\\.(?<extension>[^.]*))?");
    Matcher matcher = filenameExtensionPattern.matcher(filename);
    String baseFilename = matcher.matches() ? matcher.group("baseFilename") : null;
    Pattern baseFilenamePattern = Pattern.compile(baseFilename + "(?:\\.(?<extension>[^.]*))?");
    try (var paths = list(pathProvider.directory())) {
      List<Path> oldApprovedFiles =
          paths
              .filter(
                  path ->
                      !path.normalize().equals(approvedPath)
                          && baseFilenamePattern.matcher(path.getFileName().toString()).matches())
              .sorted(
                  comparing(
                      path -> {
                        try {
                          return getLastModifiedTime(path);
                        } catch (IOException ignored) {
                          return FileTime.from(Instant.ofEpochSecond(0));
                        }
                      }))
              .toList();
      if (oldApprovedFiles.isEmpty()) {
        return;
      }
      if (!exists(approvedPath)) {
        move(oldApprovedFiles.getLast(), approvedPath);
      }
      oldApprovedFiles.forEach(
          path -> {
            try {
              deleteIfExists(path);
            } catch (IOException ignored) {
              // this is an optional cleanup
            }
          });
    } catch (IOException ignored) {
      // this is an optional cleanup
    }
  }

  private String readApprovedFile() {
    Path approvedPath = pathProvider.approvedPath();
    try {
      return readString(approvedPath).trim();
    } catch (IOException e) {
      throw new FileApproverError("Reading approved file %s failed".formatted(approvedPath), e);
    }
  }

  private ApprovalResult check(String previouslyApproved, String receivedTrimmed) {
    ApprovalResult result =
        new FileApprovalResult(previouslyApproved, receivedTrimmed, pathProvider);
    Path receivedPath = pathProvider.receivedPath();
    if (result.needsApproval()) {
      try {
        writeString(receivedPath, receivedTrimmed + "\n", CREATE, TRUNCATE_EXISTING);
      } catch (IOException e) {
        throw new FileApproverError("Writing received to %s failed".formatted(receivedPath), e);
      }
      return result;
    }
    try {
      deleteIfExists(receivedPath);
      return result;
    } catch (IOException e) {
      throw new FileApproverError("Deleting received file %s failed".formatted(receivedPath), e);
    }
  }
}
