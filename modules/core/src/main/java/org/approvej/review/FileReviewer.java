package org.approvej.review;

import java.util.function.Function;
import org.approvej.approve.PathProvider;

/**
 * Interface for triggering a review by the user.
 *
 * <p>This usually means that a diff/merge tool is opened, which presents the difference between the
 * received and the previously approved value to users in case they differ.
 */
public interface FileReviewer extends Function<PathProvider, ReviewResult> {}
