package examples.java;

import org.approvej.approve.PathProvider;
import org.approvej.review.FileReviewResult;
import org.approvej.review.FileReviewer;
import org.approvej.review.FileReviewerProvider;
import org.approvej.review.ReviewResult;
import org.jspecify.annotations.NonNull;

public class LoggingFileReviewer implements FileReviewer, FileReviewerProvider {

  @Override
  public ReviewResult apply(PathProvider pathProvider) {
    System.out.println("Received: " + pathProvider.receivedPath());
    System.out.println("Approved: " + pathProvider.approvedPath());
    return new FileReviewResult(false);
  }

  @Override
  public @NonNull String alias() {
    return "logging";
  }

  @Override
  public FileReviewer create() {
    return new LoggingFileReviewer();
  }
}
