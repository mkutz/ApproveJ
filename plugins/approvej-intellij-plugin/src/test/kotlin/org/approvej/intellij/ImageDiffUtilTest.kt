package org.approvej.intellij

import java.awt.Color
import java.awt.image.BufferedImage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ImageDiffUtilTest {

  private val magenta = Color(255, 0, 255).rgb

  @Test
  fun identical_images() {
    val image = createImage(2, 2, Color.RED)
    val diff = ImageDiffUtil.computeDiffImage(image, image)

    assertThat(diff.width).isEqualTo(2)
    assertThat(diff.height).isEqualTo(2)
    for (y in 0 until 2) {
      for (x in 0 until 2) {
        assertThat(diff.getRGB(x, y)).isNotEqualTo(magenta)
      }
    }
  }

  @Test
  fun completely_different_images() {
    val received = createImage(2, 2, Color.RED)
    val approved = createImage(2, 2, Color.BLUE)
    val diff = ImageDiffUtil.computeDiffImage(received, approved)

    for (y in 0 until 2) {
      for (x in 0 until 2) {
        assertThat(diff.getRGB(x, y)).isEqualTo(magenta)
      }
    }
  }

  @Test
  fun single_pixel_difference() {
    val received = createImage(3, 3, Color.WHITE)
    val approved = createImage(3, 3, Color.WHITE)
    approved.setRGB(1, 1, Color.BLACK.rgb)

    val diff = ImageDiffUtil.computeDiffImage(received, approved)

    assertThat(diff.getRGB(1, 1)).isEqualTo(magenta)
    assertThat(diff.getRGB(0, 0)).isNotEqualTo(magenta)
    assertThat(diff.getRGB(2, 2)).isNotEqualTo(magenta)
  }

  @Test
  fun different_sizes() {
    val received = createImage(2, 2, Color.GREEN)
    val approved = createImage(3, 4, Color.GREEN)

    val diff = ImageDiffUtil.computeDiffImage(received, approved)

    assertThat(diff.width).isEqualTo(3)
    assertThat(diff.height).isEqualTo(4)
    // Overlapping region should match (not magenta)
    assertThat(diff.getRGB(0, 0)).isNotEqualTo(magenta)
    // Extra pixels should be magenta
    assertThat(diff.getRGB(2, 0)).isEqualTo(magenta)
    assertThat(diff.getRGB(0, 3)).isEqualTo(magenta)
  }

  private fun createImage(width: Int, height: Int, color: Color): BufferedImage {
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    for (y in 0 until height) {
      for (x in 0 until width) {
        image.setRGB(x, y, color.rgb)
      }
    }
    return image
  }
}
