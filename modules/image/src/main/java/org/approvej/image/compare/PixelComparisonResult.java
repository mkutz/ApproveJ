package org.approvej.image.compare;

import org.jspecify.annotations.NullMarked;

/**
 * Result of a pixel-by-pixel image comparison.
 *
 * @param similarity the similarity score from 0.0 to 1.0
 * @param threshold the threshold used for matching
 */
@NullMarked
public record PixelComparisonResult(double similarity, double threshold)
    implements ImageComparisonResult {

  @Override
  public boolean isMatch() {
    return similarity >= (1.0 - threshold);
  }

  @Override
  public String description() {
    return "Pixel comparison: %.2f%% similar (threshold: %.2f%%)%s"
        .formatted(similarity * 100, (1.0 - threshold) * 100, isMatch() ? "" : " - MISMATCH");
  }
}
