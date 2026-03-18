package org.approvej.image.compare;

import java.awt.image.BufferedImage;
import org.jspecify.annotations.NullMarked;

/**
 * Strategy interface for comparing two images.
 *
 * <p>Implementations provide different algorithms for determining image similarity, such as
 * pixel-by-pixel comparison or perceptual hashing.
 *
 * @see ImageComparators
 */
@NullMarked
public interface ImageComparator {

  /**
   * Compares two images and returns the comparison result.
   *
   * @param expected the expected (approved) image
   * @param actual the actual (received) image
   * @return the result of the comparison
   */
  ImageComparisonResult compare(BufferedImage expected, BufferedImage actual);
}
