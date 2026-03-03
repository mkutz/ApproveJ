package org.approvej.intellij;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Utility methods for working with ApproveJ received and approved files. */
final class ReceivedFileUtil {

  private ReceivedFileUtil() {}

  private static final Pattern RECEIVED_PATTERN =
      Pattern.compile("(?<prefix>.+)-received(?<extension>\\..+)?$");

  /** Returns {@code true} if the given filename contains {@code -received} before the extension. */
  static boolean isReceivedFileName(@NotNull String filename) {
    return RECEIVED_PATTERN.matcher(filename).matches();
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
    Matcher matcher = RECEIVED_PATTERN.matcher(filename);
    if (!matcher.matches()) return null;
    return matcher.group("prefix")
        + "-approved"
        + Objects.requireNonNullElse(matcher.group("extension"), "");
  }

  /**
   * Returns the base filename (without any {@code -received} or {@code -approved} infix) for the
   * given received filename, or {@code null} if the filename is not a received filename.
   */
  static @Nullable String toBaseFileName(@NotNull String filename) {
    Matcher matcher = RECEIVED_PATTERN.matcher(filename);
    if (!matcher.matches()) return null;
    return matcher.group("prefix") + Objects.requireNonNullElse(matcher.group("extension"), "");
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
            throw new RuntimeException("Failed to approve received file", e);
          }
        });
  }
}
