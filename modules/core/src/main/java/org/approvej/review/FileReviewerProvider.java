package org.approvej.review;

import org.approvej.configuration.Provider;
import org.jspecify.annotations.NullMarked;

/**
 * Specialized {@link Provider} for {@link FileReviewer} implementations.
 *
 * <p>This interface pre-defines the {@link #type()} method, so implementations only need to provide
 * {@link #alias()}.
 */
@NullMarked
public interface FileReviewerProvider extends Provider<FileReviewer> {

  @Override
  default Class<FileReviewer> type() {
    return FileReviewer.class;
  }
}
