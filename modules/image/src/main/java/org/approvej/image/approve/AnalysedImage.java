package org.approvej.image.approve;

import static org.approvej.image.approve.AnalysedImage.Coordinates.at;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record AnalysedImage(Dimensions dimensions, Map<Coordinates, Pixel> pixels) {

  public static AnalysedImage analyse(BufferedImage image) {
    Dimensions dimensions = Dimensions.of(image.getWidth(), image.getHeight());
    Map<Coordinates, Pixel> pixels = new HashMap<>();
    for (int y = 0; y < dimensions.height; y++) {
      for (int x = 0; x < dimensions.width; x++) {
        pixels.put(at(x, y), Pixel.of(image.getRGB(x, y)));
      }
    }
    return new AnalysedImage(dimensions, pixels);
  }

  public Pixel pixel(Coordinates coordinates) {
    return pixels.getOrDefault(coordinates, Pixel.missing());
  }

  public double difference(AnalysedImage other) {
    double difference = 0.0;
    for (int y = 0; y < dimensions.height; y++) {
      for (int x = 0; x < dimensions.width; x++) {
        Pixel thisPixel = pixel(at(x, y));
        Pixel otherPixel = other.pixel(at(x, y));
        difference += thisPixel.difference(otherPixel);
      }
    }
    return difference / (double) this.pixels.size();
  }

  public record Dimensions(int width, int height) {
    public static Dimensions of(int width, int height) {
      return new Dimensions(width, height);
    }
  }

  public record Coordinates(int x, int y) {
    public static Coordinates at(int x, int y) {
      return new Coordinates(x, y);
    }
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

    static Pixel of(int alpha, int red, int green, int blue) {
      return new ArgbPixel(
          (alpha << 24) | (red << 16) | (green << 8) | blue, alpha, red, green, blue);
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
