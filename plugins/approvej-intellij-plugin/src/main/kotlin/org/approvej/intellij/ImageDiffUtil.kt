package org.approvej.intellij

import com.intellij.ui.JBColor.MAGENTA
import java.awt.image.BufferedImage
import kotlin.math.abs

/** Computes a pixel-difference overlay image between two images. */
object ImageDiffUtil {

  private const val CHANNEL_TOLERANCE = 2
  private const val CHANNEL_MASK = 0xFF
  private const val RED_SHIFT = 16
  private const val GREEN_SHIFT = 8
  private const val ALPHA_SHIFT = 24
  private const val LUMA_RED = 0.299
  private const val LUMA_GREEN = 0.587
  private const val LUMA_BLUE = 0.114
  private const val DIM_FACTOR = 0.3

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
    @Suppress("UndesirableClassUsage") // BufferedImage is fine for pixel-level image processing
    val diff = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

    for (y in 0 until height) {
      for (x in 0 until width) {
        val outOfBoundsReceived = x >= received.width || y >= received.height
        val outOfBoundsApproved = x >= approved.width || y >= approved.height

        if (outOfBoundsReceived || outOfBoundsApproved) {
          diff.setRGB(x, y, MAGENTA.rgb)
          continue
        }

        val receivedRgb = received.getRGB(x, y)
        val approvedRgb = approved.getRGB(x, y)

        if (pixelsMatch(receivedRgb, approvedRgb)) {
          diff.setRGB(x, y, dimmedGrayscale(receivedRgb))
        } else {
          diff.setRGB(x, y, MAGENTA.rgb)
        }
      }
    }
    return diff
  }

  private fun pixelsMatch(rgb1: Int, rgb2: Int): Boolean {
    val r1 = (rgb1 shr RED_SHIFT) and CHANNEL_MASK
    val g1 = (rgb1 shr GREEN_SHIFT) and CHANNEL_MASK
    val b1 = rgb1 and CHANNEL_MASK
    val r2 = (rgb2 shr RED_SHIFT) and CHANNEL_MASK
    val g2 = (rgb2 shr GREEN_SHIFT) and CHANNEL_MASK
    val b2 = rgb2 and CHANNEL_MASK
    return abs(r1 - r2) <= CHANNEL_TOLERANCE &&
      abs(g1 - g2) <= CHANNEL_TOLERANCE &&
      abs(b1 - b2) <= CHANNEL_TOLERANCE
  }

  private fun dimmedGrayscale(rgb: Int): Int {
    val r = (rgb shr RED_SHIFT) and CHANNEL_MASK
    val g = (rgb shr GREEN_SHIFT) and CHANNEL_MASK
    val b = rgb and CHANNEL_MASK
    val gray = ((LUMA_RED * r + LUMA_GREEN * g + LUMA_BLUE * b) * DIM_FACTOR).toInt()
    return (CHANNEL_MASK shl ALPHA_SHIFT) or (gray shl RED_SHIFT) or (gray shl GREEN_SHIFT) or gray
  }
}
