package org.approvej.intellij

import java.awt.Color
import java.awt.image.BufferedImage

/** Computes a pixel-difference overlay image between two images. */
object ImageDiffUtil {

  private const val CHANNEL_TOLERANCE = 2
  private val MAGENTA = Color(255, 0, 255).rgb

  /**
   * Produces a diff image highlighting pixel differences between [received] and [approved].
   *
   * Matching pixels are rendered as dimmed grayscale for spatial context. Differing pixels are
   * rendered in magenta. If the images have different dimensions, the output uses the larger
   * dimensions and treats missing pixels as fully different.
   */
  fun computeDiffImage(received: BufferedImage, approved: BufferedImage): BufferedImage {
    val width = maxOf(received.width, approved.width)
    val height = maxOf(received.height, approved.height)
    val diff = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

    for (y in 0 until height) {
      for (x in 0 until width) {
        val outOfBoundsReceived = x >= received.width || y >= received.height
        val outOfBoundsApproved = x >= approved.width || y >= approved.height

        if (outOfBoundsReceived || outOfBoundsApproved) {
          diff.setRGB(x, y, MAGENTA)
          continue
        }

        val receivedRgb = received.getRGB(x, y)
        val approvedRgb = approved.getRGB(x, y)

        if (pixelsMatch(receivedRgb, approvedRgb)) {
          diff.setRGB(x, y, dimmedGrayscale(receivedRgb))
        } else {
          diff.setRGB(x, y, MAGENTA)
        }
      }
    }
    return diff
  }

  private fun pixelsMatch(rgb1: Int, rgb2: Int): Boolean {
    val r1 = (rgb1 shr 16) and 0xFF
    val g1 = (rgb1 shr 8) and 0xFF
    val b1 = rgb1 and 0xFF
    val r2 = (rgb2 shr 16) and 0xFF
    val g2 = (rgb2 shr 8) and 0xFF
    val b2 = rgb2 and 0xFF
    return Math.abs(r1 - r2) <= CHANNEL_TOLERANCE &&
      Math.abs(g1 - g2) <= CHANNEL_TOLERANCE &&
      Math.abs(b1 - b2) <= CHANNEL_TOLERANCE
  }

  private fun dimmedGrayscale(rgb: Int): Int {
    val r = (rgb shr 16) and 0xFF
    val g = (rgb shr 8) and 0xFF
    val b = rgb and 0xFF
    val gray = ((0.299 * r + 0.587 * g + 0.114 * b) * 0.3).toInt()
    return (0xFF shl 24) or (gray shl 16) or (gray shl 8) or gray
  }
}
