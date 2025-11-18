package org.approvej.image;

import static org.approvej.Configuration.configuration;
import static org.approvej.image.approve.ImageFileApprover.imageFile;

import java.awt.*;
import java.awt.image.BufferedImage;
import org.approvej.ApprovalBuilder;
import org.approvej.approve.PathProvider;
import org.approvej.image.approve.ImageFileApprover;
import org.approvej.review.FileReviewer;
import org.approvej.review.ReviewResult;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class ImageApprovalBuilder {

  private final BufferedImage value;
  private final String name;
  private final String filenameExtension;
  @Nullable private final FileReviewer fileReviewer;

  private ImageApprovalBuilder(
      BufferedImage image,
      String name,
      String filenameExtension,
      @Nullable FileReviewer fileReviewer) {
    this.value = image;
    this.name = name;
    this.filenameExtension = filenameExtension;
    this.fileReviewer = fileReviewer;
  }

  /**
   * Creates a new builder for the given value.
   *
   * @param value the value to approve
   * @return a new {@link ApprovalBuilder} for the given value
   */
  public static ImageApprovalBuilder approveImage(BufferedImage value) {
    return new ImageApprovalBuilder(value, "", "png", configuration.defaultFileReviewer());
  }

  public void byFile(PathProvider pathProvider) {
    PathProvider updatedPathProvider =
        pathProvider.filenameAffix(name).filenameExtension(filenameExtension);
    ImageFileApprover approver = imageFile(updatedPathProvider);
    ImageApprovalResult approvalResult = approver.apply(value);
    if (approvalResult.needsApproval() && fileReviewer != null) {
      ReviewResult reviewResult = fileReviewer.apply(updatedPathProvider);
      if (reviewResult.needsReapproval()) {
        approvalResult = approver.apply(value);
      }
    }
    approvalResult.throwIfNotApproved();
  }
}
