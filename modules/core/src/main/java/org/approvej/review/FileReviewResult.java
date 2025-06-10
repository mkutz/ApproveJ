package org.approvej.review;

/**
 * Result class for {@link FileReviewer}s.
 *
 * @param needsReapproval indicates if the result should be reapproved after the review (e.g.
 *     because the approval file was modified)
 */
public record FileReviewResult(boolean needsReapproval) implements ReviewResult {}
