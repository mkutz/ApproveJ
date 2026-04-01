package examples.java;

import org.approvej.approve.PathProvider;
import org.approvej.review.ReviewResult;
import org.approvej.review.ReviewResultRecord;
import org.approvej.review.Reviewer;
import org.approvej.review.ReviewerProvider;
import org.jspecify.annotations.NonNull;

public class LoggingReviewer implements Reviewer, ReviewerProvider {

  @Override
  public ReviewResult apply(PathProvider pathProvider) {
    System.out.println("Received: " + pathProvider.receivedPath());
    System.out.println("Approved: " + pathProvider.approvedPath());
    return new ReviewResultRecord(false);
  }

  @Override
  public @NonNull String alias() {
    return "logging";
  }

  @Override
  public Reviewer create() {
    return new LoggingReviewer();
  }
}
