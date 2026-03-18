package org.approvej.image.compare;

import org.jspecify.annotations.NullMarked;

/**
 * Factory methods to create {@link ImageComparator} instances.
 *
 * <p>Provides access to different image comparison strategies:
 *
 * <ul>
 *   <li>{@link #perceptualHash()} - Perceptual hash comparison, robust to anti-aliasing and minor
 *       rendering differences
 *   <li>{@link #pixel()} - Pixel-by-pixel comparison for exact matching
 * </ul>
 */
@NullMarked
public final class ImageComparators {

  private ImageComparators() {}

  /**
   * Creates a perceptual hash comparator with 90% similarity threshold.
   *
   * <p>Perceptual hashing is robust to antialiasing, font rendering variations, and minor visual
   * differences that are imperceptible to humans.
   *
   * @return a new {@link PerceptualHashComparator} with default threshold
   */
  public static PerceptualHashComparator perceptualHash() {
    return new PerceptualHashComparator(0.90);
  }

  /**
   * Creates a pixel-by-pixel comparator with 1% difference threshold.
   *
   * <p>Pixel comparison compares each pixel individually and is suitable for exact-match use cases
   * where precision is critical.
   *
   * @return a new {@link PixelComparator} with default threshold
   */
  public static PixelComparator pixel() {
    return new PixelComparator(0.01);
  }
}
