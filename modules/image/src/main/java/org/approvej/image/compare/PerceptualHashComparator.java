package org.approvej.image.compare;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import org.jspecify.annotations.NullMarked;

/**
 * Compares images using perceptual hashing (pHash).
 *
 * <p>Perceptual hashing is robust to anti-aliasing, font rendering variations, compression
 * artifacts, and minor visual differences that are imperceptible to humans. It works by:
 *
 * <ol>
 *   <li>Resizing the image to 32x32 pixels
 *   <li>Converting to grayscale
 *   <li>Applying a Discrete Cosine Transform (DCT)
 *   <li>Extracting the top-left 8x8 low-frequency components
 *   <li>Creating a 64-bit hash based on whether each value is above or below the mean
 *   <li>Comparing hashes using Hamming distance
 * </ol>
 */
@NullMarked
public final class PerceptualHashComparator implements ImageComparator {

  private static final int RESIZE_SIZE = 32;
  private static final int HASH_SIZE = 8;
  private static final int HASH_BITS = HASH_SIZE * HASH_SIZE;

  private final double threshold;

  PerceptualHashComparator(double threshold) {
    this.threshold = threshold;
  }

  /**
   * Returns a new comparator with the specified threshold.
   *
   * @param threshold the minimum similarity required for a match (0.0 to 1.0), where 0.90 means 90%
   *     similarity required
   * @return a new comparator with the specified threshold
   */
  public PerceptualHashComparator withThreshold(double threshold) {
    return new PerceptualHashComparator(threshold);
  }

  @Override
  public ImageComparisonResult compare(BufferedImage expected, BufferedImage actual) {
    long expectedHash = computeHash(expected);
    long actualHash = computeHash(actual);

    int hammingDistance = Long.bitCount(expectedHash ^ actualHash);
    double similarity = 1.0 - ((double) hammingDistance / HASH_BITS);

    return new PerceptualHashComparisonResult(similarity, threshold, hammingDistance);
  }

  private long computeHash(BufferedImage image) {
    BufferedImage resized = resize(image, RESIZE_SIZE, RESIZE_SIZE);
    double[][] grayscale = toGrayscale(resized);
    double[][] dct = applyDct(grayscale);
    return computeHashFromDct(dct);
  }

  private BufferedImage resize(BufferedImage image, int width, int height) {
    BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = resized.createGraphics();
    g.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.drawImage(image, 0, 0, width, height, null);
    g.dispose();
    return resized;
  }

  private double[][] toGrayscale(BufferedImage image) {
    int width = image.getWidth();
    int height = image.getHeight();
    double[][] grayscale = new double[height][width];

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int rgb = image.getRGB(x, y);
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        // Luminosity formula for grayscale conversion
        grayscale[y][x] = 0.299 * r + 0.587 * g + 0.114 * b;
      }
    }

    return grayscale;
  }

  private double[][] applyDct(double[][] input) {
    int n = input.length;
    double[][] dct = new double[n][n];

    // Precompute cosine values for efficiency
    double[][] cosValues = new double[n][n];
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        cosValues[i][j] = Math.cos((2 * j + 1) * i * Math.PI / (2 * n));
      }
    }

    for (int u = 0; u < n; u++) {
      for (int v = 0; v < n; v++) {
        double sum = 0.0;
        for (int y = 0; y < n; y++) {
          for (int x = 0; x < n; x++) {
            sum += input[y][x] * cosValues[u][y] * cosValues[v][x];
          }
        }

        double cu = (u == 0) ? 1.0 / Math.sqrt(2) : 1.0;
        double cv = (v == 0) ? 1.0 / Math.sqrt(2) : 1.0;
        dct[u][v] = 0.25 * cu * cv * sum;
      }
    }

    return dct;
  }

  private long computeHashFromDct(double[][] dct) {
    // Extract top-left 8x8 (excluding DC component at [0][0])
    // Calculate mean of low-frequency components
    double sum = 0.0;
    for (int y = 0; y < HASH_SIZE; y++) {
      for (int x = 0; x < HASH_SIZE; x++) {
        if (y == 0 && x == 0) continue; // Skip DC component
        sum += dct[y][x];
      }
    }
    double mean = sum / (HASH_BITS - 1);

    // Build hash: bit is 1 if value > mean
    long hash = 0;
    for (int y = 0; y < HASH_SIZE; y++) {
      for (int x = 0; x < HASH_SIZE; x++) {
        if (y == 0 && x == 0) continue;
        hash <<= 1;
        if (dct[y][x] > mean) {
          hash |= 1;
        }
      }
    }

    return hash;
  }
}
