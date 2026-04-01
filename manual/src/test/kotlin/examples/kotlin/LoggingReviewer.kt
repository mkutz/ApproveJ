package examples.kotlin

import org.approvej.approve.PathProvider
import org.approvej.review.ReviewResult
import org.approvej.review.ReviewResultRecord
import org.approvej.review.Reviewer
import org.approvej.review.ReviewerProvider

class LoggingReviewer : Reviewer, ReviewerProvider {
  override fun apply(pathProvider: PathProvider): ReviewResult {
    println("Received: " + pathProvider.receivedPath())
    println("Approved: " + pathProvider.approvedPath())
    return ReviewResultRecord(false)
  }

  override fun alias() = "logging"

  override fun create() = LoggingReviewer()
}
