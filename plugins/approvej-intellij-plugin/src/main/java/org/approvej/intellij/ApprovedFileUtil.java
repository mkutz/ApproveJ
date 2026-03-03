package org.approvej.intellij;

import com.intellij.openapi.vfs.VirtualFile;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Utility methods for detecting ApproveJ approved files. */
final class ApprovedFileUtil {

  private ApprovedFileUtil() {}

  private static final Pattern APPROVED_PATTERN =
      Pattern.compile("(?<prefix>.+)-approved(?<extension>\\..++)?$");

  /** Returns {@code true} if the given filename contains {@code -approved} before the extension. */
  static boolean isApprovedFileName(@NotNull String filename) {
    return APPROVED_PATTERN.matcher(filename).matches();
  }

  /**
   * Returns {@code true} if the given file's name contains {@code -approved} before the extension.
   */
  static boolean isApprovedFile(@Nullable VirtualFile file) {
    return file != null && isApprovedFileName(file.getName());
  }
}
