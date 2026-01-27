package examples.kotlin

import org.approvej.approve.PathProvider
import org.approvej.review.FileReviewResult
import org.approvej.review.FileReviewer
import org.approvej.review.FileReviewerProvider
import org.approvej.review.ReviewResult

class LoggingFileReviewer : FileReviewer, FileReviewerProvider {
  override fun apply(pathProvider: PathProvider): ReviewResult {
    println("Received: " + pathProvider.receivedPath())
    println("Approved: " + pathProvider.approvedPath())
    return FileReviewResult(false)
  }

  override fun alias() = "logging"

  override fun create() = LoggingFileReviewer()
}
