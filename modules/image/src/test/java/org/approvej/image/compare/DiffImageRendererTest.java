package org.approvej.image.compare;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class DiffImageRendererTest {

  @Test
  void computeDiffImage_identical_images() {
    BufferedImage image = createSolidImage(10, 10, Color.RED);

    BufferedImage diff = DiffImageRenderer.computeDiffImage(image, image);

    assertThat(diff.getWidth()).isEqualTo(10);
    assertThat(diff.getHeight()).isEqualTo(10);
    assertThat(diff.getRGB(0, 0)).isNotEqualTo(Color.MAGENTA.getRGB());
  }

  @Test
  void computeDiffImage_different_images() {
    BufferedImage received = createSolidImage(10, 10, Color.RED);
    BufferedImage approved = createSolidImage(10, 10, Color.BLUE);

    BufferedImage diff = DiffImageRenderer.computeDiffImage(received, approved);

    assertThat(diff.getRGB(0, 0)).isEqualTo(Color.MAGENTA.getRGB());
    assertThat(diff.getRGB(5, 5)).isEqualTo(Color.MAGENTA.getRGB());
  }

  @Test
  void computeDiffImage_different_dimensions() {
    BufferedImage received = createSolidImage(10, 10, Color.RED);
    BufferedImage approved = createSolidImage(20, 15, Color.RED);

    BufferedImage diff = DiffImageRenderer.computeDiffImage(received, approved);

    assertThat(diff.getWidth()).isEqualTo(20);
    assertThat(diff.getHeight()).isEqualTo(15);
    assertThat(diff.getRGB(15, 12)).isEqualTo(Color.MAGENTA.getRGB());
  }

  @Test
  void computeDiffImage_within_tolerance() {
    BufferedImage received = createSolidImage(5, 5, new Color(100, 100, 100));
    BufferedImage approved = createSolidImage(5, 5, new Color(101, 99, 100));

    BufferedImage diff = DiffImageRenderer.computeDiffImage(received, approved);

    assertThat(diff.getRGB(0, 0)).isNotEqualTo(Color.MAGENTA.getRGB());
  }

  @Test
  void computeDiffImage_beyond_tolerance() {
    BufferedImage received = createSolidImage(5, 5, new Color(100, 100, 100));
    BufferedImage approved = createSolidImage(5, 5, new Color(104, 100, 100));

    BufferedImage diff = DiffImageRenderer.computeDiffImage(received, approved);

    assertThat(diff.getRGB(0, 0)).isEqualTo(Color.MAGENTA.getRGB());
  }

  @Test
  void computeDiffImage_matching_pixels_are_dimmed_grayscale() {
    BufferedImage image = createSolidImage(5, 5, Color.WHITE);

    BufferedImage diff = DiffImageRenderer.computeDiffImage(image, image);

    int pixel = diff.getRGB(0, 0);
    int red = (pixel >> 16) & 0xFF;
    int green = (pixel >> 8) & 0xFF;
    int blue = pixel & 0xFF;
    assertThat(red).isEqualTo(green).isEqualTo(blue);
    assertThat(red).isLessThan(255);
  }

  private static BufferedImage createSolidImage(int width, int height, Color color) {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        image.setRGB(x, y, color.getRGB());
      }
    }
    return image;
  }
}
