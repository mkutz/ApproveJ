package org.approvej.intellij

import com.intellij.openapi.vfs.VirtualFile

object ApprovedFileUtil {

  private const val APPROVED_INFIX = "-approved"

  /**
   * Finds the last occurrence of `-approved` in the filename and returns the index, or `-1` if the
   * filename does not contain the infix in a valid position (not at the start, and followed only by
   * an optional extension).
   */
  private fun findApprovedIndex(filename: String): Int {
    val index = filename.lastIndexOf(APPROVED_INFIX)
    if (index <= 0) return -1
    val suffix = filename.substring(index + APPROVED_INFIX.length)
    return if (suffix.isEmpty() || suffix[0] == '.') index else -1
  }

  /** Returns `true` if the given filename contains `-approved` before the extension. */
  fun isApprovedFileName(filename: String): Boolean = findApprovedIndex(filename) > 0

  /** Returns `true` if the given file's name contains `-approved` before the extension. */
  fun isApprovedFile(file: VirtualFile?): Boolean = file != null && isApprovedFileName(file.name)

  /**
   * Returns the sibling received [VirtualFile] for the given approved file, or `null` if no
   * received file exists.
   */
  fun findReceivedFile(approvedFile: VirtualFile): VirtualFile? {
    val parent = approvedFile.parent ?: return null
    val filename = approvedFile.name
    val index = findApprovedIndex(filename)
    if (index < 0) return null
    val receivedName =
      filename.substring(0, index) + "-received" + filename.substring(index + APPROVED_INFIX.length)
    return parent.findChild(receivedName)
  }
}
