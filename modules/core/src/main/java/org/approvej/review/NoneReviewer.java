package org.approvej.review;

import org.approvej.approve.PathProvider;
import org.jspecify.annotations.NullMarked;

/** A {@link Reviewer} that does nothing and never triggers a reapproval. */
@NullMarked
public record NoneReviewer() implements Reviewer, ReviewerProvider {

  @Override
  public ReviewResult apply(PathProvider pathProvider) {
    return new ReviewResultRecord(false);
  }

  @Override
  public String alias() {
    return "none";
  }

  @Override
  public Reviewer create() {
    return new NoneReviewer();
  }
}
