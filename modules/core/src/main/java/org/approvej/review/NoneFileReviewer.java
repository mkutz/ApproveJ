package org.approvej.review;

import org.approvej.approve.PathProvider;
import org.jspecify.annotations.NullMarked;

/** A {@link FileReviewer} that does nothing and never triggers a reapproval. */
@NullMarked
public record NoneFileReviewer() implements FileReviewer, FileReviewerProvider {

  /** The alias for this reviewer used in configuration. */
  public static final String ALIAS = "none";

  @Override
  public ReviewResult apply(PathProvider pathProvider) {
    return new FileReviewResult(false);
  }

  @Override
  public String alias() {
    return ALIAS;
  }

  @Override
  public FileReviewer create() {
    return new NoneFileReviewer();
  }
}
