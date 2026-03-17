package org.approvej.intellij

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile

object ReceivedFileUtil {

  private val LOG = logger<ReceivedFileUtil>()

  val RECEIVED_FILE_KEY: Key<VirtualFile> = Key.create("ApproveJ.receivedFile")
  val APPROVED_FILE_KEY: Key<VirtualFile> = Key.create("ApproveJ.approvedFile")

  private const val RECEIVED_INFIX = "-received"

  /**
   * Finds the last occurrence of `-received` in the filename and returns the index, or `-1` if the
   * filename does not contain the infix in a valid position (not at the start, and followed only by
   * an optional extension).
   */
  private fun findReceivedIndex(filename: String): Int {
    val index = filename.lastIndexOf(RECEIVED_INFIX)
    if (index <= 0) return -1
    val suffix = filename.substring(index + RECEIVED_INFIX.length)
    return if (suffix.isEmpty() || suffix[0] == '.') index else -1
  }

  /** Returns `true` if the given filename contains `-received` before the extension. */
  fun isReceivedFileName(filename: String): Boolean = findReceivedIndex(filename) > 0

  /** Returns `true` if the given file's name contains `-received` before the extension. */
  fun isReceivedFile(file: VirtualFile?): Boolean = file != null && isReceivedFileName(file.name)

  /**
   * Returns the approved filename for the given received filename, or `null` if the filename is not
   * a received filename.
   */
  fun toApprovedFileName(filename: String): String? {
    val index = findReceivedIndex(filename)
    if (index < 0) return null
    return filename.substring(0, index) +
      "-approved" +
      filename.substring(index + RECEIVED_INFIX.length)
  }

  /**
   * Returns the base filename (without any `-received` or `-approved` infix) for the given received
   * filename, or `null` if the filename is not a received filename.
   */
  fun toBaseFileName(filename: String): String? {
    val index = findReceivedIndex(filename)
    if (index < 0) return null
    return filename.substring(0, index) + filename.substring(index + RECEIVED_INFIX.length)
  }

  /**
   * Returns an ordered list of candidate approved filenames for the given received filename:
   * approved name first, then base name (without `-approved`/`-received` infix).
   */
  fun approvedFileNameCandidates(receivedFileName: String): List<String> {
    val approvedName = toApprovedFileName(receivedFileName) ?: return emptyList()
    val baseName = toBaseFileName(receivedFileName) ?: return emptyList()
    return listOf(approvedName, baseName)
  }

  /**
   * Returns the sibling approved [VirtualFile] for the given received file, or `null` if no
   * approved file exists. Checks for both the default `-approved` infix and a custom approved file
   * without the infix.
   */
  fun findApprovedFile(receivedFile: VirtualFile): VirtualFile? {
    val parent = receivedFile.parent ?: return null
    for (candidate in approvedFileNameCandidates(receivedFile.name)) {
      val approvedFile = parent.findChild(candidate)
      if (approvedFile != null) return approvedFile
    }
    return null
  }

  /**
   * Returns `true` if the event's virtual file is a received file with an existing approved
   * counterpart.
   */
  fun isActionAvailable(event: AnActionEvent): Boolean {
    val file = event.getData(CommonDataKeys.VIRTUAL_FILE)
    return isReceivedFile(file) && findApprovedFile(file!!) != null
  }

  /**
   * Extracts the received and approved files from the event and passes them to the given action.
   * Does nothing if the files cannot be resolved.
   */
  fun withReceivedAndApproved(
    event: AnActionEvent,
    action: (received: VirtualFile, approved: VirtualFile) -> Unit,
  ) {
    val file = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
    if (event.project == null) return
    val approvedFile = findApprovedFile(file) ?: return
    action(file, approvedFile)
  }

  /** Opens IntelliJ's diff viewer comparing the received file with the approved file. */
  fun openDiff(project: Project, receivedFile: VirtualFile, approvedFile: VirtualFile) {
    val contentFactory = DiffContentFactory.getInstance()
    val request =
      SimpleDiffRequest(
        "ApproveJ: ${receivedFile.name}",
        contentFactory.create(project, receivedFile),
        contentFactory.create(project, approvedFile),
        "Received: ${receivedFile.name}",
        "Approved: ${approvedFile.name}",
      )
    request.putUserData(RECEIVED_FILE_KEY, receivedFile)
    request.putUserData(APPROVED_FILE_KEY, approvedFile)
    DiffManager.getInstance().showDiff(project, request)
  }

  /**
   * Copies the received file content to the approved file and deletes the received file. The
   * operation is wrapped in a [WriteCommandAction] to support undo.
   */
  fun approve(project: Project, receivedFile: VirtualFile, approvedFile: VirtualFile) {
    WriteCommandAction.runWriteCommandAction(
      project,
      "Approve Received",
      null,
      {
        try {
          approvedFile.setBinaryContent(receivedFile.contentsToByteArray())
          receivedFile.delete(ReceivedFileUtil::class.java)
        } catch (e: java.io.IOException) {
          LOG.error("Failed to approve received file", e)
        }
      },
    )
  }

  /**
   * Deletes the received file without copying its content to the approved file. The operation is
   * wrapped in a [WriteCommandAction] to support undo.
   */
  fun reject(project: Project, receivedFile: VirtualFile) {
    WriteCommandAction.runWriteCommandAction(
      project,
      "Reject Received",
      null,
      {
        try {
          receivedFile.delete(ReceivedFileUtil::class.java)
        } catch (e: java.io.IOException) {
          LOG.error("Failed to reject received file", e)
        }
      },
    )
  }
}
