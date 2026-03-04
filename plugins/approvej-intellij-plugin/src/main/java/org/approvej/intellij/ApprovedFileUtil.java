package org.approvej.intellij;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Utility methods for detecting ApproveJ approved files. */
final class ApprovedFileUtil {

  private ApprovedFileUtil() {}

  private static final String APPROVED_INFIX = "-approved";

  /**
   * Finds the last occurrence of {@code -approved} in the filename and returns the index, or {@code
   * -1} if the filename does not contain the infix in a valid position (not at the start, and
   * followed only by an optional extension).
   */
  private static int findApprovedIndex(@NotNull String filename) {
    int index = filename.lastIndexOf(APPROVED_INFIX);
    if (index <= 0) return -1;
    String suffix = filename.substring(index + APPROVED_INFIX.length());
    if (suffix.isEmpty() || suffix.charAt(0) == '.') {
      return index;
    }
    return -1;
  }

  /** Returns {@code true} if the given filename contains {@code -approved} before the extension. */
  static boolean isApprovedFileName(@NotNull String filename) {
    return findApprovedIndex(filename) > 0;
  }

  /**
   * Returns {@code true} if the given file's name contains {@code -approved} before the extension.
   */
  static boolean isApprovedFile(@Nullable VirtualFile file) {
    return file != null && isApprovedFileName(file.getName());
  }

  /**
   * Returns the sibling received {@link VirtualFile} for the given approved file, or {@code null}
   * if no received file exists.
   */
  static @Nullable VirtualFile findReceivedFile(@NotNull VirtualFile approvedFile) {
    VirtualFile parent = approvedFile.getParent();
    if (parent == null) return null;
    String filename = approvedFile.getName();
    int index = findApprovedIndex(filename);
    if (index < 0) return null;
    String receivedName =
        filename.substring(0, index)
            + "-received"
            + filename.substring(index + APPROVED_INFIX.length());
    return parent.findChild(receivedName);
  }
}
