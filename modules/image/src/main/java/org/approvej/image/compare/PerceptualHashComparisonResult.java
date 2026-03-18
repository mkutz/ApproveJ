package org.approvej.image.compare;

import org.jspecify.annotations.NullMarked;

/**
 * Result of a perceptual hash (pHash) image comparison.
 *
 * @param similarity the similarity score from 0.0 to 1.0
 * @param threshold the minimum similarity required for a match
 * @param hammingDistance the Hamming distance between the two hashes (0-64)
 */
@NullMarked
public record PerceptualHashComparisonResult(
    double similarity, double threshold, int hammingDistance) implements ImageComparisonResult {

  @Override
  public boolean isMatch() {
    return similarity >= threshold;
  }

  @Override
  public String description() {
    return "Perceptual hash: %.2f%% similar (threshold: %.2f%%, Hamming distance: %d/64)%s"
        .formatted(
            similarity * 100, threshold * 100, hammingDistance, isMatch() ? "" : " - MISMATCH");
  }
}
