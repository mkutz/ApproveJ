package org.approvej.image.compare;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class PerceptualHashComparatorTest {

  private static final BufferedImage SCREENSHOT;
  private static final BufferedImage BLACK;
  private static final BufferedImage WHITE;
  private static final BufferedImage RED;
  private static final BufferedImage GREEN;

  static {
    try {
      SCREENSHOT =
          ImageIO.read(
              requireNonNull(
                  PerceptualHashComparatorTest.class.getResourceAsStream("/screenshot.png")));
      BLACK =
          ImageIO.read(
              requireNonNull(PerceptualHashComparatorTest.class.getResourceAsStream("/black.png")));
      WHITE =
          ImageIO.read(
              requireNonNull(PerceptualHashComparatorTest.class.getResourceAsStream("/white.png")));
      RED =
          ImageIO.read(
              requireNonNull(PerceptualHashComparatorTest.class.getResourceAsStream("/red.png")));
      GREEN =
          ImageIO.read(
              requireNonNull(PerceptualHashComparatorTest.class.getResourceAsStream("/green.png")));
    } catch (IOException e) {
      throw new RuntimeException("Failed to load test images", e);
    }
  }

  @Test
  void identicalImages_match() {
    PerceptualHashComparator comparator = ImageComparators.perceptualHash();

    ImageComparisonResult result = comparator.compare(SCREENSHOT, SCREENSHOT);

    assertThat(result.isMatch()).isTrue();
    assertThat(result.similarity()).isEqualTo(1.0);
  }

  @Test
  void blackAndWhite_noMatch() {
    PerceptualHashComparator comparator = ImageComparators.perceptualHash();

    ImageComparisonResult result = comparator.compare(BLACK, WHITE);

    assertThat(result.isMatch()).isFalse();
    assertThat(result.similarity()).isLessThan(0.9);
  }

  @Test
  void differentColors_lowSimilarity() {
    PerceptualHashComparator comparator = ImageComparators.perceptualHash();

    ImageComparisonResult result = comparator.compare(RED, GREEN);

    assertThat(result.similarity()).isLessThan(0.9);
  }

  @Test
  void withThreshold_customThreshold() {
    PerceptualHashComparator comparator = ImageComparators.perceptualHash().withThreshold(0.95);

    ImageComparisonResult result = comparator.compare(SCREENSHOT, SCREENSHOT);

    assertThat(result.isMatch()).isTrue();
  }

  @Test
  void description_containsInfo() {
    PerceptualHashComparator comparator = ImageComparators.perceptualHash();

    ImageComparisonResult result = comparator.compare(RED, GREEN);

    assertThat(result.description()).contains("Perceptual hash");
    assertThat(result.description()).contains("similar");
    assertThat(result.description()).contains("Hamming distance");
  }

  @Test
  void hammingDistance_identicalImages() {
    PerceptualHashComparator comparator = ImageComparators.perceptualHash();

    PerceptualHashComparisonResult result =
        (PerceptualHashComparisonResult) comparator.compare(SCREENSHOT, SCREENSHOT);

    assertThat(result.hammingDistance()).isZero();
  }
}
