package org.approvej.image;

import static java.util.Objects.requireNonNull;
import static org.approvej.approve.PathProviders.nextToTest;
import static org.approvej.image.ImageApprovalBuilder.approveImage;
import static org.approvej.image.scrub.ImageScrubbers.region;
import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import org.approvej.approve.ApprovedFileInventoryUpdater;
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

  @Test
  void named() throws IOException {
    BufferedImage image =
        ImageIO.read(requireNonNull(getClass().getResourceAsStream("/screenshot.png")));

    approveImage(image).named("custom_name").byFile();
  }

  @Test
  void byFile_registers_in_inventory() throws Exception {
    BufferedImage image =
        ImageIO.read(requireNonNull(getClass().getResourceAsStream("/screenshot.png")));

    approveImage(image).byFile();

    var collectedField = ApprovedFileInventoryUpdater.class.getDeclaredField("collected");
    collectedField.setAccessible(true);
    @SuppressWarnings("unchecked")
    var collected = (ConcurrentHashMap<Path, ?>) collectedField.get(null);
    assertThat(collected.keySet())
        .anyMatch(path -> path.toString().contains("byFile_registers_in_inventory-approved.png"));
  }
}
