package org.approvej.image.compare;

import org.jspecify.annotations.NullMarked;

/**
 * Result of comparing two images using an {@link ImageComparator}.
 *
 * <p>Provides information about whether the images match according to the comparator's criteria,
 * the similarity score, and a human-readable description of the comparison.
 */
@NullMarked
public interface ImageComparisonResult {

  /**
   * Returns whether the images match according to the comparator's threshold.
   *
   * @return true if the images are considered matching
   */
  boolean isMatch();

  /**
   * Returns the similarity score between 0.0 and 1.0, where 1.0 means identical.
   *
   * @return similarity score from 0.0 (completely different) to 1.0 (identical)
   */
  double similarity();

  /**
   * Returns a human-readable description of the comparison result.
   *
   * @return description of the comparison result
   */
  String description();
}
