package org.approvej.image.scrub;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.UnaryOperator;
import org.jspecify.annotations.NullMarked;

/**
 * Factory for image scrubbers that mask regions of an image.
 *
 * <p>Image scrubbers are useful for hiding dynamic content (like version numbers, timestamps, or
 * ads) that would otherwise cause approval tests to fail.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * approveImage(screenshot)
 *     .scrubbedOf(region(10, 50, 100, 20))
 *     .byFile();
 * }</pre>
 */
@NullMarked
public final class ImageScrubbers {

  /** The default color used to mask scrubbed regions (magenta). */
  public static final Color DEFAULT_SCRUB_COLOR = Color.MAGENTA;

  private ImageScrubbers() {}

  /**
   * Creates a scrubber that masks a single rectangular region.
   *
   * @param x the x coordinate of the region's top-left corner
   * @param y the y coordinate of the region's top-left corner
   * @param width the width of the region
   * @param height the height of the region
   * @return a scrubber that masks the specified region
   */
  public static UnaryOperator<BufferedImage> region(int x, int y, int width, int height) {
    return region(new Rectangle(x, y, width, height));
  }

  /**
   * Creates a scrubber that masks a single rectangular region.
   *
   * @param rectangle the region to mask
   * @return a scrubber that masks the specified region
   */
  public static UnaryOperator<BufferedImage> region(Rectangle rectangle) {
    return regions(DEFAULT_SCRUB_COLOR, rectangle);
  }

  /**
   * Creates a scrubber that masks a single rectangular region with a custom color.
   *
   * @param color the color to use for masking
   * @param x the x coordinate of the region's top-left corner
   * @param y the y coordinate of the region's top-left corner
   * @param width the width of the region
   * @param height the height of the region
   * @return a scrubber that masks the specified region
   */
  public static UnaryOperator<BufferedImage> region(
      Color color, int x, int y, int width, int height) {
    return regions(color, new Rectangle(x, y, width, height));
  }

  /**
   * Creates a scrubber that masks multiple rectangular regions.
   *
   * @param rectangles the regions to mask
   * @return a scrubber that masks the specified regions
   */
  public static UnaryOperator<BufferedImage> regions(Rectangle... rectangles) {
    return regions(DEFAULT_SCRUB_COLOR, rectangles);
  }

  /**
   * Creates a scrubber that masks multiple rectangular regions with a custom color.
   *
   * @param color the color to use for masking
   * @param rectangles the regions to mask
   * @return a scrubber that masks the specified regions
   */
  public static UnaryOperator<BufferedImage> regions(Color color, Rectangle... rectangles) {
    return image -> {
      BufferedImage copy = copyImage(image);
      Graphics2D g = copy.createGraphics();
      try {
        g.setColor(color);
        for (Rectangle rect : rectangles) {
          g.fillRect(rect.x, rect.y, rect.width, rect.height);
        }
      } finally {
        g.dispose();
      }
      return copy;
    };
  }

  private static BufferedImage copyImage(BufferedImage source) {
    BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
    Graphics g = copy.getGraphics();
    try {
      g.drawImage(source, 0, 0, null);
    } finally {
      g.dispose();
    }
    return copy;
  }
}
