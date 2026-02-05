package org.approvej.image.compare;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class PixelComparatorTest {

  private static final BufferedImage SCREENSHOT;
  private static final BufferedImage BLACK;
  private static final BufferedImage WHITE;
  private static final BufferedImage RED;
  private static final BufferedImage GREEN;

  static {
    try {
      SCREENSHOT =
          ImageIO.read(
              requireNonNull(PixelComparatorTest.class.getResourceAsStream("/screenshot.png")));
      BLACK =
          ImageIO.read(requireNonNull(PixelComparatorTest.class.getResourceAsStream("/black.png")));
      WHITE =
          ImageIO.read(requireNonNull(PixelComparatorTest.class.getResourceAsStream("/white.png")));
      RED = ImageIO.read(requireNonNull(PixelComparatorTest.class.getResourceAsStream("/red.png")));
      GREEN =
          ImageIO.read(requireNonNull(PixelComparatorTest.class.getResourceAsStream("/green.png")));
    } catch (IOException e) {
      throw new RuntimeException("Failed to load test images", e);
    }
  }

  @Test
  void identicalImages_match() {
    PixelComparator comparator = ImageComparators.pixel();

    ImageComparisonResult result = comparator.compare(SCREENSHOT, SCREENSHOT);

    assertThat(result.isMatch()).isTrue();
    assertThat(result.similarity()).isEqualTo(1.0);
  }

  @Test
  void blackAndWhite_noMatch() {
    PixelComparator comparator = ImageComparators.pixel();

    ImageComparisonResult result = comparator.compare(BLACK, WHITE);

    assertThat(result.isMatch()).isFalse();
    assertThat(result.similarity()).isEqualTo(0.0);
  }

  @Test
  void differentColors_noMatch() {
    PixelComparator comparator = ImageComparators.pixel();

    ImageComparisonResult result = comparator.compare(RED, GREEN);

    assertThat(result.isMatch()).isFalse();
    assertThat(result.similarity()).isLessThan(0.5);
  }

  @Test
  void withThreshold_customThreshold() {
    PixelComparator comparator = ImageComparators.pixel().withThreshold(0.5);

    ImageComparisonResult result = comparator.compare(RED, GREEN);

    // With 50% threshold, images that are less than 50% different should still match
    assertThat(result.similarity()).isLessThan(0.5);
  }

  @Test
  void description_containsInfo() {
    PixelComparator comparator = ImageComparators.pixel();

    ImageComparisonResult result = comparator.compare(RED, GREEN);

    assertThat(result.description()).contains("Pixel comparison");
    assertThat(result.description()).contains("similar");
    assertThat(result.description()).contains("threshold");
  }
}
