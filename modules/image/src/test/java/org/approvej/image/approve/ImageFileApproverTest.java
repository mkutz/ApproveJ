package org.approvej.image.approve;

import static java.util.Objects.requireNonNull;
import static org.approvej.approve.PathProviders.nextToTest;
import static org.approvej.image.compare.ImageComparators.perceptualHash;
import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ImageFileApproverTest {

  @Test
  void apply() throws IOException {
    BufferedImage image =
        ImageIO.read(requireNonNull(getClass().getResourceAsStream("/screenshot.png")));
    ImageFileApprover approver = new ImageFileApprover(nextToTest(), perceptualHash());

    var result = approver.apply(image);

    assertThat(result).isNotNull();
  }
}
