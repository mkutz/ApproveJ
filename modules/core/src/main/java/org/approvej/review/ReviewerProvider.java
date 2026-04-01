package org.approvej.review;

import org.approvej.configuration.Provider;
import org.jspecify.annotations.NullMarked;

/**
 * Specialized {@link Provider} for {@link Reviewer} implementations.
 *
 * <p>This interface pre-defines the {@link #type()} method, so implementations only need to provide
 * {@link #alias()}.
 */
@NullMarked
public interface ReviewerProvider extends Provider<Reviewer> {

  @Override
  default Class<Reviewer> type() {
    return Reviewer.class;
  }
}
