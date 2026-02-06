package org.approvej.image.approve;

import org.approvej.approve.PathProvider;
import org.approvej.image.ImageApprovalError;
import org.approvej.image.ImageApprovalResult;
import org.approvej.image.compare.ImageComparisonResult;

/**
 * {@link ImageApprovalResult} for image files.
 *
 * @param comparisonResult the result of comparing the images
 * @param pathProvider the {@link PathProvider} providing the paths to the received and approved
 *     files
 */
public record ImageFileApprovalResult(
    ImageComparisonResult comparisonResult, PathProvider pathProvider)
    implements ImageApprovalResult {

  @Override
  public boolean needsApproval() {
    return !comparisonResult.isMatch();
  }

  @Override
  public void throwIfNotApproved() {
    if (needsApproval()) {
      throw new ImageApprovalError(comparisonResult.description());
    }
  }
}
