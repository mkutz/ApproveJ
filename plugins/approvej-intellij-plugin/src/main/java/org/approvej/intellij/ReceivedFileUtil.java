package org.approvej.intellij;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.IOException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Utility methods for working with ApproveJ received and approved files. */
final class ReceivedFileUtil {

  private ReceivedFileUtil() {}

  private static final String RECEIVED = "-received";
  private static final String APPROVED = "-approved";

  /** Returns {@code true} if the given filename contains {@code -received} before the extension. */
  static boolean isReceivedFileName(@Nullable String filename) {
    return receivedIndex(filename) >= 0;
  }

  /**
   * Returns {@code true} if the given file's name contains {@code -received} before the extension.
   */
  static boolean isReceivedFile(@Nullable VirtualFile file) {
    return file != null && isReceivedFileName(file.getName());
  }

  /**
   * Returns the approved filename for the given received filename, or {@code null} if the filename
   * is not a received filename.
   */
  static @Nullable String toApprovedFileName(@Nullable String filename) {
    int index = receivedIndex(filename);
    if (index < 0) return null;
    return filename.substring(0, index) + APPROVED + filename.substring(index + RECEIVED.length());
  }

  /**
   * Returns the base filename (without any {@code -received} or {@code -approved} infix) for the
   * given received filename, or {@code null} if the filename is not a received filename.
   */
  static @Nullable String toBaseFileName(@Nullable String filename) {
    int index = receivedIndex(filename);
    if (index < 0) return null;
    return filename.substring(0, index) + filename.substring(index + RECEIVED.length());
  }

  /**
   * Returns an ordered list of candidate approved filenames for the given received filename:
   * approved name first, then base name (without {@code -approved}/{@code -received} infix).
   */
  static @NotNull List<String> approvedFileNameCandidates(@NotNull String receivedFileName) {
    String approvedName = toApprovedFileName(receivedFileName);
    String baseName = toBaseFileName(receivedFileName);
    if (approvedName == null || baseName == null) return List.of();
    return List.of(approvedName, baseName);
  }

  /**
   * Returns the sibling approved {@link VirtualFile} for the given received file, or {@code null}
   * if no approved file exists. Checks for both the default {@code -approved} infix and a custom
   * approved file without the infix.
   */
  static @Nullable VirtualFile findApprovedFile(@NotNull VirtualFile receivedFile) {
    VirtualFile parent = receivedFile.getParent();
    if (parent == null) return null;
    for (String candidate : approvedFileNameCandidates(receivedFile.getName())) {
      VirtualFile approvedFile = parent.findChild(candidate);
      if (approvedFile != null) return approvedFile;
    }
    return null;
  }

  /** Opens IntelliJ's diff viewer comparing the received file with the approved file. */
  static void openDiff(@NotNull Project project, @NotNull VirtualFile receivedFile) {
    VirtualFile approvedFile = findApprovedFile(receivedFile);
    var contentFactory = DiffContentFactory.getInstance();
    var receivedContent = contentFactory.create(project, receivedFile);
    var approvedContent =
        approvedFile != null
            ? contentFactory.create(project, approvedFile)
            : contentFactory.createEmpty();
    var request =
        new SimpleDiffRequest(
            "ApproveJ: " + receivedFile.getName(),
            approvedContent,
            receivedContent,
            approvedFile != null ? approvedFile.getName() : "(no approved file)",
            receivedFile.getName());
    DiffManager.getInstance().showDiff(project, request);
  }

  /**
   * Copies the received file content to the approved file (creating it if needed) and deletes the
   * received file. The operation is wrapped in a {@link WriteCommandAction} to support undo.
   */
  static void approve(@NotNull Project project, @NotNull VirtualFile receivedFile) {
    String approvedName = toApprovedFileName(receivedFile.getName());
    if (approvedName == null) return;
    WriteCommandAction.runWriteCommandAction(
        project,
        "Approve Received",
        null,
        () -> {
          try {
            VirtualFile parent = receivedFile.getParent();
            if (parent == null) {
              return;
            }
            VirtualFile approvedFile = findApprovedFile(receivedFile);
            if (approvedFile == null) {
              approvedFile = parent.createChildData(ReceivedFileUtil.class, approvedName);
            }
            approvedFile.setBinaryContent(receivedFile.contentsToByteArray());
            receivedFile.delete(ReceivedFileUtil.class);
          } catch (IOException e) {
            throw new RuntimeException("Failed to approve received file", e);
          }
        });
  }

  private static int receivedIndex(String filename) {
    if (filename == null) return -1;
    int receivedDotIndex = filename.lastIndexOf(RECEIVED + ".");
    if (receivedDotIndex >= 0) return receivedDotIndex;
    if (filename.endsWith(RECEIVED)) return filename.length() - RECEIVED.length();
    return -1;
  }
}
