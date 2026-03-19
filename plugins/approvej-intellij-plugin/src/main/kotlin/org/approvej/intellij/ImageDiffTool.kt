package org.approvej.intellij

import com.intellij.diff.DiffContext
import com.intellij.diff.FrameDiffTool
import com.intellij.diff.requests.DiffRequest
import com.intellij.openapi.fileEditor.FileEditorManager
import java.awt.BorderLayout
import java.awt.GridLayout
import java.io.File
import javax.imageio.ImageIO
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingConstants

/** A three-panel diff tool that shows Received, Pixel Difference, and Approved images. */
class ImageDiffTool : FrameDiffTool {

  override fun getName(): String = "ApproveJ Image Diff"

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

      val imagesPanel = JPanel(GridLayout(1, 3))

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

    override fun dispose() {}

    private fun createImagePanel(title: String, image: java.awt.image.BufferedImage): JScrollPane {
      val label = JLabel(ImageIcon(image))
      val scrollPane = JScrollPane(label)
      scrollPane.border = BorderFactory.createTitledBorder(title)
      return scrollPane
    }
  }

  companion object {
    private val IMAGE_EXTENSIONS = setOf("png", "jpg", "jpeg", "gif", "bmp", "webp")

    private fun isImageFile(filename: String): Boolean {
      val extension = filename.substringAfterLast('.', "").lowercase()
      return extension in IMAGE_EXTENSIONS
    }
  }
}
