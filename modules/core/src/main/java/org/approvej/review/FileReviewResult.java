package org.approvej.review;

public record FileReviewResult(boolean needsReapproval) implements ReviewResult {}
