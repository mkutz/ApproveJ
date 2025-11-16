package org.approvej.image.approve;

import static org.approvej.image.approve.AnalysedImage.analyse;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.approvej.approve.PathProvider;
import org.approvej.image.ImageApprovalError;
import org.approvej.image.ImageApprovalResult;

/**
 * {@link ImageApprovalResult} for image files.
 *
 * @param pathProvider the {@link PathProvider} providing the paths to the received an approved
 *     files
 */
public record ImageFileApprovalResult(BufferedImage previouslyApproved, BufferedImage received, PathProvider pathProvider)
    implements ImageApprovalResult {

  @Override
  public boolean needsApproval() {
    AnalysedImage previouslyApprovedAnalysed = analyse(previouslyApproved);
    AnalysedImage receivedAnalysed = analyse(received);

    if (!previouslyApprovedAnalysed.dimensions().equals(receivedAnalysed.dimensions())) {
      return true;
    }

    return previouslyApprovedAnalysed.difference(receivedAnalysed) > 0.01;
  }

  @Override
  public void throwIfNotApproved() {
    if (needsApproval()) {
      throw new ImageApprovalError(null);
    }
  }
}
