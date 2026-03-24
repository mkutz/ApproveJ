package org.approvej.intellij

import com.intellij.diff.DiffContext
import com.intellij.diff.DiffTool
import com.intellij.diff.FrameDiffTool
import com.intellij.diff.SuppressiveDiffTool
import com.intellij.diff.requests.DiffRequest
import com.intellij.diff.tools.binary.BinaryDiffTool
import com.intellij.diff.tools.simple.SimpleDiffTool
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

private val IMAGE_EXTENSIONS = setOf("png", "jpg", "jpeg", "gif", "bmp", "webp")
private const val IMAGE_COLUMNS = 3

internal fun isImageFile(filename: String): Boolean {
  val extension = filename.substringAfterLast('.', "").lowercase()
  return extension in IMAGE_EXTENSIONS
}

/** A three-panel diff tool that shows Received, Pixel Difference, and Approved images. */
class ImageDiffTool : FrameDiffTool, SuppressiveDiffTool {

  override fun getName(): String = "ApproveJ Image Diff"

  override fun getSuppressedTools(): List<Class<out DiffTool>> =
    listOf(SimpleDiffTool::class.java, BinaryDiffTool::class.java)

  override fun canShow(context: DiffContext, request: DiffRequest): Boolean {
    val receivedFile = request.getUserData(ReceivedFileUtil.RECEIVED_FILE_KEY) ?: return false
    val approvedFile = request.getUserData(ReceivedFileUtil.APPROVED_FILE_KEY) ?: return false
    return isImageFile(receivedFile.name) && isImageFile(approvedFile.name)
  }

  override fun createComponent(
    context: DiffContext,
    request: DiffRequest,
  ): FrameDiffTool.DiffViewer = ImageDiffViewer(context, request)

  class ImageDiffViewer(context: DiffContext, request: DiffRequest) : FrameDiffTool.DiffViewer {

    private val rootPanel = JPanel(BorderLayout())

    init {
      val receivedFile = request.getUserData(ReceivedFileUtil.RECEIVED_FILE_KEY)!!
      val approvedFile = request.getUserData(ReceivedFileUtil.APPROVED_FILE_KEY)!!
      val project = context.project

      if (project != null) {
        val testMethod = InventoryUtil.findTestMethod(approvedFile, project)
        val notificationPanel =
          ApproveJDiffExtension.createNotificationPanel(
            project,
            receivedFile,
            approvedFile,
            testMethod,
          ) {
            FileEditorManager.getInstance(project).openFile(approvedFile, true)
          }
        rootPanel.add(notificationPanel, BorderLayout.NORTH)
      }

      val receivedImage = ImageIO.read(File(receivedFile.path))
      val approvedImage = ImageIO.read(File(approvedFile.path))

      val imagesPanel = JPanel(GridLayout(1, IMAGE_COLUMNS))

      if (receivedImage != null && approvedImage != null) {
        val diffImage = ImageDiffUtil.computeDiffImage(receivedImage, approvedImage)
        imagesPanel.add(createImagePanel("Received", receivedImage))
        imagesPanel.add(createImagePanel("Pixel Difference", diffImage))
        imagesPanel.add(createImagePanel("Approved", approvedImage))
      } else {
        val label = JLabel("Unable to read image files", SwingConstants.CENTER)
        imagesPanel.add(label)
      }

      rootPanel.add(imagesPanel, BorderLayout.CENTER)
    }

    override fun getComponent() = rootPanel

    override fun getPreferredFocusedComponent() = rootPanel

    override fun init(): FrameDiffTool.ToolbarComponents = FrameDiffTool.ToolbarComponents()

    override fun dispose() {
      // nothing to dispose
    }

    private fun createImagePanel(title: String, image: BufferedImage): JBScrollPane {
      val label = JLabel(ImageIcon(image))
      val scrollPane = JBScrollPane(label)
      scrollPane.border = BorderFactory.createTitledBorder(title)
      return scrollPane
    }
  }
}
