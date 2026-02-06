package org.approvej.image;

import static java.util.Objects.requireNonNull;
import static org.approvej.approve.PathProviders.nextToTest;
import static org.approvej.image.ImageApprovalBuilder.approveImage;
import static org.approvej.image.scrub.ImageScrubbers.region;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ImageApprovalBuilderTest {

  @Test
  void success() throws IOException {
    BufferedImage image =
        ImageIO.read(requireNonNull(getClass().getResourceAsStream("/screenshot.png")));

    approveImage(image).byFile(nextToTest().filenameExtension("png"));
  }

  @Test
  void scrubbedOf() throws IOException {
    BufferedImage image =
        ImageIO.read(requireNonNull(getClass().getResourceAsStream("/screenshot.png")));

    approveImage(image).scrubbedOf(region(10, 10, 50, 20)).byFile();
  }
}
