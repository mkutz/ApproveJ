package org.approvej.intellij;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Utility methods for working with ApproveJ received and approved files. */
final class ReceivedFileUtil {

  private static final Logger LOG = Logger.getInstance(ReceivedFileUtil.class);

  private ReceivedFileUtil() {}

  private static final String RECEIVED_INFIX = "-received";

  /**
   * Finds the last occurrence of {@code -received} in the filename and returns the index, or {@code
   * -1} if the filename does not contain the infix in a valid position (not at the start, and
   * followed only by an optional extension).
   */
  private static int findReceivedIndex(@NotNull String filename) {
    int index = filename.lastIndexOf(RECEIVED_INFIX);
    if (index <= 0) return -1;
    String suffix = filename.substring(index + RECEIVED_INFIX.length());
    if (suffix.isEmpty() || suffix.charAt(0) == '.') {
      return index;
    }
    return -1;
  }

  /** Returns {@code true} if the given filename contains {@code -received} before the extension. */
  static boolean isReceivedFileName(@NotNull String filename) {
    return findReceivedIndex(filename) > 0;
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
  static @Nullable String toApprovedFileName(@NotNull String filename) {
    int index = findReceivedIndex(filename);
    if (index < 0) return null;
    return filename.substring(0, index)
        + "-approved"
        + filename.substring(index + RECEIVED_INFIX.length());
  }

  /**
   * Returns the base filename (without any {@code -received} or {@code -approved} infix) for the
   * given received filename, or {@code null} if the filename is not a received filename.
   */
  static @Nullable String toBaseFileName(@NotNull String filename) {
    int index = findReceivedIndex(filename);
    if (index < 0) return null;
    return filename.substring(0, index) + filename.substring(index + RECEIVED_INFIX.length());
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

  /**
   * Returns {@code true} if the event's virtual file is a received file with an existing approved
   * counterpart.
   */
  static boolean isActionAvailable(@NotNull AnActionEvent event) {
    VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
    return isReceivedFile(file) && findApprovedFile(file) != null;
  }

  /**
   * Extracts the received and approved files from the event and passes them to the given action.
   * Does nothing if the files cannot be resolved.
   */
  static void withReceivedAndApproved(
      @NotNull AnActionEvent event, @NotNull BiConsumer<VirtualFile, VirtualFile> action) {
    VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
    if (file == null || event.getProject() == null) return;
    VirtualFile approvedFile = findApprovedFile(file);
    if (approvedFile == null) return;
    action.accept(file, approvedFile);
  }

  /** Opens IntelliJ's diff viewer comparing the received file with the approved file. */
  static void openDiff(
      @NotNull Project project,
      @NotNull VirtualFile receivedFile,
      @NotNull VirtualFile approvedFile) {
    var contentFactory = DiffContentFactory.getInstance();
    var request =
        new SimpleDiffRequest(
            "ApproveJ: " + receivedFile.getName(),
            contentFactory.create(project, approvedFile),
            contentFactory.create(project, receivedFile),
            approvedFile.getName(),
            receivedFile.getName());
    DiffManager.getInstance().showDiff(project, request);
  }

  /**
   * Copies the received file content to the approved file and deletes the received file. The
   * operation is wrapped in a {@link WriteCommandAction} to support undo.
   */
  static void approve(
      @NotNull Project project,
      @NotNull VirtualFile receivedFile,
      @NotNull VirtualFile approvedFile) {
    WriteCommandAction.runWriteCommandAction(
        project,
        "Approve Received",
        null,
        () -> {
          try {
            approvedFile.setBinaryContent(receivedFile.contentsToByteArray());
            receivedFile.delete(ReceivedFileUtil.class);
          } catch (IOException e) {
            LOG.error("Failed to approve received file", e);
          }
        });
  }
}
