package org.approvej.image.approve;

import java.awt.image.BufferedImage;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record AnalysedImage(BufferedImage image, int width, int height, int size) {

  public static AnalysedImage analyse(BufferedImage image) {
    int width = image.getWidth();
    int height = image.getHeight();
    return new AnalysedImage(image, width, height, width * height);
  }

  public Pixel pixel(int x, int y) {
    if (x >= image.getWidth() || y >= image.getHeight()) return Pixel.missing();
    return Pixel.of(image.getRGB(x, y));
  }

  public double difference(AnalysedImage other) {
    double difference = 0.0;
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        Pixel thisPixel = pixel(x, y);
        Pixel otherPixel = other.pixel(x, y);
        difference += thisPixel.difference(otherPixel);
      }
    }
    return difference / (double) (image.getWidth() * image.getHeight());
  }

  public boolean isMoreDifferentThan(AnalysedImage other, double maxDifference) {
    double difference = 0.0;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        Pixel thisPixel = pixel(x, y);
        Pixel otherPixel = other.pixel(x, y);
        difference += thisPixel.difference(otherPixel) / size;
        if (difference > maxDifference) return true;
      }
    }
    return false;
  }

  public interface Pixel {

    int MAX_VALUE = 0xff;
    Pixel MISSING = new MissingPixel();

    static Pixel of(int argb) {
      return new ArgbPixel(
          argb,
          (argb >> 24) & MAX_VALUE,
          (argb >> 16) & MAX_VALUE,
          (argb >> 8) & MAX_VALUE,
          argb & MAX_VALUE);
    }

    static Pixel missing() {
      return MISSING;
    }

    int alpha();

    int red();

    int green();

    int blue();

    double difference(Pixel pixel);
  }

  public record ArgbPixel(int argb, int alpha, int red, int green, int blue) implements Pixel {

    @Override
    public double difference(Pixel other) {
      if (other instanceof MissingPixel) {
        return 1.0;
      }
      double colorDiff =
          (Math.abs(this.red - other.red())
                  + Math.abs(this.green - other.green())
                  + Math.abs(this.blue - other.blue()))
              / (double) (MAX_VALUE * 3);

      double alphaWeight = ((this.alpha + other.alpha()) / 2.0) / MAX_VALUE;
      return colorDiff * alphaWeight;
    }
  }

  public record MissingPixel() implements Pixel {

    @Override
    public int alpha() {
      return -1;
    }

    @Override
    public int red() {
      return -1;
    }

    @Override
    public int green() {
      return -1;
    }

    @Override
    public int blue() {
      return -1;
    }

    @Override
    public double difference(Pixel pixel) {
      return 1;
    }
  }
}
