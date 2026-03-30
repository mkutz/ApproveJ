package org.approvej.image.compare;

import java.awt.Color;
import java.awt.image.BufferedImage;
import org.jspecify.annotations.NullMarked;

/**
 * Renders a pixel-difference overlay image between two images.
 *
 * <p>Matching pixels are rendered as dimmed grayscale for spatial context. Differing pixels are
 * rendered in magenta. If the images have different dimensions, the output uses the larger
 * dimensions and treats missing pixels as fully different.
 */
@NullMarked
public final class DiffImageRenderer {

  private static final int CHANNEL_TOLERANCE = 2;
  private static final int CHANNEL_MASK = 0xFF;
  private static final int RED_SHIFT = 16;
  private static final int GREEN_SHIFT = 8;
  private static final int ALPHA_SHIFT = 24;
  private static final double LUMA_RED = 0.299;
  private static final double LUMA_GREEN = 0.587;
  private static final double LUMA_BLUE = 0.114;
  private static final double DIM_FACTOR = 0.3;
  private static final int HIGHLIGHT_COLOR = Color.MAGENTA.getRGB();

  private DiffImageRenderer() {}

  /**
   * Produces a diff image highlighting pixel differences between the received and approved images.
   *
   * @param received the received (actual) image
   * @param approved the previously approved (expected) image
   * @return a new image showing differences in magenta and matching areas as dimmed grayscale
   */
  public static BufferedImage computeDiffImage(BufferedImage received, BufferedImage approved) {
    int width = Math.max(received.getWidth(), approved.getWidth());
    int height = Math.max(received.getHeight(), approved.getHeight());
    BufferedImage diff = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        boolean outOfBoundsReceived = x >= received.getWidth() || y >= received.getHeight();
        boolean outOfBoundsApproved = x >= approved.getWidth() || y >= approved.getHeight();

        if (outOfBoundsReceived || outOfBoundsApproved) {
          diff.setRGB(x, y, HIGHLIGHT_COLOR);
          continue;
        }

        int receivedRgb = received.getRGB(x, y);
        int approvedRgb = approved.getRGB(x, y);

        if (pixelsMatch(receivedRgb, approvedRgb)) {
          diff.setRGB(x, y, dimmedGrayscale(receivedRgb));
        } else {
          diff.setRGB(x, y, HIGHLIGHT_COLOR);
        }
      }
    }
    return diff;
  }

  private static boolean pixelsMatch(int rgb1, int rgb2) {
    int alpha1 = (rgb1 >> ALPHA_SHIFT) & CHANNEL_MASK;
    int red1 = (rgb1 >> RED_SHIFT) & CHANNEL_MASK;
    int green1 = (rgb1 >> GREEN_SHIFT) & CHANNEL_MASK;
    int blue1 = rgb1 & CHANNEL_MASK;
    int alpha2 = (rgb2 >> ALPHA_SHIFT) & CHANNEL_MASK;
    int red2 = (rgb2 >> RED_SHIFT) & CHANNEL_MASK;
    int green2 = (rgb2 >> GREEN_SHIFT) & CHANNEL_MASK;
    int blue2 = rgb2 & CHANNEL_MASK;
    return Math.abs(alpha1 - alpha2) <= CHANNEL_TOLERANCE
        && Math.abs(red1 - red2) <= CHANNEL_TOLERANCE
        && Math.abs(green1 - green2) <= CHANNEL_TOLERANCE
        && Math.abs(blue1 - blue2) <= CHANNEL_TOLERANCE;
  }

  private static int dimmedGrayscale(int rgb) {
    int alpha = (rgb >> ALPHA_SHIFT) & CHANNEL_MASK;
    int red = (rgb >> RED_SHIFT) & CHANNEL_MASK;
    int green = (rgb >> GREEN_SHIFT) & CHANNEL_MASK;
    int blue = rgb & CHANNEL_MASK;
    int gray = (int) ((LUMA_RED * red + LUMA_GREEN * green + LUMA_BLUE * blue) * DIM_FACTOR);
    return (alpha << ALPHA_SHIFT) | (gray << RED_SHIFT) | (gray << GREEN_SHIFT) | gray;
  }
}
