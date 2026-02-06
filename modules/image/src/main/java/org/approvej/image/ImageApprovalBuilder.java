package org.approvej.image;

import static org.approvej.configuration.Configuration.configuration;
import static org.approvej.image.approve.ImageFileApprover.imageFile;
import static org.approvej.image.compare.ImageComparators.perceptualHash;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.UnaryOperator;
import javax.imageio.ImageIO;
import org.approvej.approve.PathProvider;
import org.approvej.approve.PathProviders;
import org.approvej.image.approve.ImageFileApprover;
import org.approvej.image.compare.ImageComparator;
import org.approvej.review.FileReviewer;
import org.approvej.review.ReviewResult;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ImageApprovalBuilder {

  private final BufferedImage value;
  private final String name;
  private final String filenameExtension;
  private final FileReviewer fileReviewer;
  private final ImageComparator comparator;

  private ImageApprovalBuilder(
      BufferedImage image,
      String name,
      String filenameExtension,
      FileReviewer fileReviewer,
      ImageComparator comparator) {
    this.value = image;
    this.name = name;
    this.filenameExtension = filenameExtension;
    this.fileReviewer = fileReviewer;
    this.comparator = comparator;
  }

  /**
   * Creates a new builder for the given image.
   *
   * <p>By default, uses perceptual hash comparison with 90% similarity threshold.
   *
   * @param value the image to approve
   * @return a new {@link ImageApprovalBuilder} for the given image
   */
  public static ImageApprovalBuilder approveImage(BufferedImage value) {
    return new ImageApprovalBuilder(
        value, "", "png", configuration.defaultFileReviewer(), perceptualHash());
  }

  /**
   * Creates a new builder for the given image bytes.
   *
   * <p>This is a convenience method for use with screenshot APIs that return byte arrays, such as
   * Playwright's {@code page.screenshot()}.
   *
   * <p>By default, uses perceptual hash comparison with 90% similarity threshold.
   *
   * @param imageBytes the image bytes (PNG, JPEG, etc.) to approve
   * @return a new {@link ImageApprovalBuilder} for the given image
   * @throws UncheckedIOException if the image bytes cannot be read
   */
  public static ImageApprovalBuilder approveImage(byte[] imageBytes) {
    try {
      BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
      if (image == null) {
        throw new UncheckedIOException(
            new IOException("Failed to read image from bytes - unsupported format"));
      }
      return approveImage(image);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read image from bytes", e);
    }
  }

  /**
   * Specifies the image comparator to use for comparing images.
   *
   * @param comparator the {@link ImageComparator} to use
   * @return a new builder with the specified comparator
   * @see org.approvej.image.compare.ImageComparators
   */
  public ImageApprovalBuilder comparedBy(ImageComparator comparator) {
    return new ImageApprovalBuilder(value, name, filenameExtension, fileReviewer, comparator);
  }

  /**
   * Applies a scrubber to mask regions of the image before comparison.
   *
   * <p>This is useful for hiding dynamic content like version numbers, timestamps, or ads that
   * would otherwise cause approval tests to fail.
   *
   * <p>Example:
   *
   * <pre>{@code
   * approveImage(screenshot)
   *     .scrubbedOf(region(10, 50, 100, 20))
   *     .byFile();
   * }</pre>
   *
   * @param scrubber a function that modifies the image to mask dynamic regions
   * @return a new builder with the scrubbed image
   * @see org.approvej.image.scrub.ImageScrubbers
   */
  public ImageApprovalBuilder scrubbedOf(UnaryOperator<BufferedImage> scrubber) {
    return new ImageApprovalBuilder(
        scrubber.apply(value), name, filenameExtension, fileReviewer, comparator);
  }

  /**
   * Approves the image by comparing it to a file next to the test class.
   *
   * <p>This is equivalent to calling {@code byFile(PathProviders.nextToTest())}.
   *
   * @see PathProviders#nextToTest()
   */
  public void byFile() {
    byFile(PathProviders.nextToTest());
  }

  /**
   * Approves the image by comparing it to a file at the specified path.
   *
   * @param pathProvider the {@link PathProvider} to determine the paths of the approved and
   *     received files
   */
  public void byFile(PathProvider pathProvider) {
    PathProvider updatedPathProvider =
        pathProvider.filenameAffix(name).filenameExtension(filenameExtension);
    ImageFileApprover approver = imageFile(updatedPathProvider, comparator);
    ImageApprovalResult approvalResult = approver.apply(value);
    if (approvalResult.needsApproval()) {
      ReviewResult reviewResult = fileReviewer.apply(updatedPathProvider);
      if (reviewResult.needsReapproval()) {
        approvalResult = approver.apply(value);
      }
    }
    approvalResult.throwIfNotApproved();
  }
}
