package org.approvej.image.scrub;

import static org.approvej.image.scrub.ImageScrubbers.DEFAULT_SCRUB_COLOR;
import static org.approvej.image.scrub.ImageScrubbers.region;
import static org.approvej.image.scrub.ImageScrubbers.regions;
import static org.assertj.core.api.Assertions.assertThat;

import java.awt.*;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class ImageScrubbersTest {

  @Test
  void region_masks_rectangular_area() {
    BufferedImage image = createWhiteImage(100, 100);

    BufferedImage scrubbed = region(10, 20, 30, 40).apply(image);

    assertThat(scrubbed.getRGB(15, 30)).isEqualTo(DEFAULT_SCRUB_COLOR.getRGB());
    assertThat(scrubbed.getRGB(0, 0)).isEqualTo(Color.WHITE.getRGB());
  }

  @Test
  void region_does_not_modify_original_image() {
    BufferedImage image = createWhiteImage(100, 100);

    region(10, 20, 30, 40).apply(image);

    assertThat(image.getRGB(15, 30)).isEqualTo(Color.WHITE.getRGB());
  }

  @Test
  void region_with_rectangle() {
    BufferedImage image = createWhiteImage(100, 100);

    BufferedImage scrubbed = region(new Rectangle(10, 20, 30, 40)).apply(image);

    assertThat(scrubbed.getRGB(15, 30)).isEqualTo(DEFAULT_SCRUB_COLOR.getRGB());
  }

  @Test
  void region_with_custom_color() {
    BufferedImage image = createWhiteImage(100, 100);

    BufferedImage scrubbed = region(Color.RED, 10, 20, 30, 40).apply(image);

    assertThat(scrubbed.getRGB(15, 30)).isEqualTo(Color.RED.getRGB());
  }

  @Test
  void regions_masks_multiple_areas() {
    BufferedImage image = createWhiteImage(100, 100);

    BufferedImage scrubbed =
        regions(new Rectangle(10, 10, 20, 20), new Rectangle(50, 50, 20, 20)).apply(image);

    assertThat(scrubbed.getRGB(15, 15)).isEqualTo(DEFAULT_SCRUB_COLOR.getRGB());
    assertThat(scrubbed.getRGB(55, 55)).isEqualTo(DEFAULT_SCRUB_COLOR.getRGB());
    assertThat(scrubbed.getRGB(35, 35)).isEqualTo(Color.WHITE.getRGB());
  }

  @Test
  void regions_with_custom_color() {
    BufferedImage image = createWhiteImage(100, 100);

    BufferedImage scrubbed =
        regions(Color.BLUE, new Rectangle(10, 10, 20, 20), new Rectangle(50, 50, 20, 20))
            .apply(image);

    assertThat(scrubbed.getRGB(15, 15)).isEqualTo(Color.BLUE.getRGB());
    assertThat(scrubbed.getRGB(55, 55)).isEqualTo(Color.BLUE.getRGB());
  }

  private static BufferedImage createWhiteImage(int width, int height) {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics g = image.getGraphics();
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, width, height);
    g.dispose();
    return image;
  }
}
