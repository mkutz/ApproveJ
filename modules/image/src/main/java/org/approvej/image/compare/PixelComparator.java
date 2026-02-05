package org.approvej.image.compare;

import java.awt.image.BufferedImage;
import org.jspecify.annotations.NullMarked;

/**
 * Compares images pixel-by-pixel.
 *
 * <p>This comparator calculates the difference for each pixel based on RGB values and returns the
 * overall similarity as a percentage. It is suitable for exact-match use cases but may be sensitive
 * to anti-aliasing and rendering differences.
 */
@NullMarked
public final class PixelComparator implements ImageComparator {

  private static final int MAX_VALUE = 0xff;

  private final double threshold;

  PixelComparator(double threshold) {
    this.threshold = threshold;
  }

  /**
   * Returns a new comparator with the specified threshold.
   *
   * @param threshold the maximum allowed difference (0.0 to 1.0), where 0.01 means 1% difference is
   *     acceptable
   * @return a new comparator with the specified threshold
   */
  public PixelComparator withThreshold(double threshold) {
    return new PixelComparator(threshold);
  }

  @Override
  public ImageComparisonResult compare(BufferedImage expected, BufferedImage actual) {
    int width = Math.max(expected.getWidth(), actual.getWidth());
    int height = Math.max(expected.getHeight(), actual.getHeight());
    int size = width * height;

    double totalDifference = 0.0;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        double pixelDiff = pixelDifference(expected, actual, x, y);
        totalDifference += pixelDiff / size;
      }
    }

    double similarity = 1.0 - totalDifference;
    return new PixelComparisonResult(similarity, threshold);
  }

  private double pixelDifference(BufferedImage expected, BufferedImage actual, int x, int y) {
    boolean expectedMissing = x >= expected.getWidth() || y >= expected.getHeight();
    boolean actualMissing = x >= actual.getWidth() || y >= actual.getHeight();

    if (expectedMissing || actualMissing) {
      return 1.0;
    }

    int expectedRgb = expected.getRGB(x, y);
    int actualRgb = actual.getRGB(x, y);

    int expectedAlpha = (expectedRgb >> 24) & MAX_VALUE;
    int expectedRed = (expectedRgb >> 16) & MAX_VALUE;
    int expectedGreen = (expectedRgb >> 8) & MAX_VALUE;
    int expectedBlue = expectedRgb & MAX_VALUE;

    int actualAlpha = (actualRgb >> 24) & MAX_VALUE;
    int actualRed = (actualRgb >> 16) & MAX_VALUE;
    int actualGreen = (actualRgb >> 8) & MAX_VALUE;
    int actualBlue = actualRgb & MAX_VALUE;

    double colorDiff =
        (Math.abs(expectedRed - actualRed)
                + Math.abs(expectedGreen - actualGreen)
                + Math.abs(expectedBlue - actualBlue))
            / (double) (MAX_VALUE * 3);

    double alphaWeight = ((expectedAlpha + actualAlpha) / 2.0) / MAX_VALUE;
    return colorDiff * alphaWeight;
  }
}
