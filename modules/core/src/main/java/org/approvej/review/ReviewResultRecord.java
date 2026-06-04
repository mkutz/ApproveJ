package org.approvej.review;

import org.jspecify.annotations.NullMarked;

/**
 * Result class for {@link Reviewer}s.
 *
 * @param needsReapproval indicates if the result should be reapproved after the review (e.g.
 *     because the approval file was modified)
 */
@NullMarked
public record ReviewResultRecord(boolean needsReapproval) implements ReviewResult {}
