package org.approvej.image.approve;

import static java.util.Objects.requireNonNull;
import static org.approvej.approve.PathProviders.nextToTest;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ImageFileApproverTest {

  @Test
  public void apply() throws IOException {
    Image image = ImageIO.read(requireNonNull(getClass().getResourceAsStream("/example.png")));

    ImageFileApprover approver = new ImageFileApprover(nextToTest());

    // approver.apply(image);
  }
}
