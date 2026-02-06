package org.approvej.image.approve;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class AnalysedImageTest {

  private static final AnalysedImage SCREENSHOT;
  private static final AnalysedImage BLACK;
  private static final AnalysedImage WHITE;
  private static final AnalysedImage RED;
  private static final AnalysedImage GREEN;
  private static final AnalysedImage BLUE;
  private static final AnalysedImage CYAN;
  private static final AnalysedImage MAGENTA;
  private static final AnalysedImage YELLOW;

  static {
    try {
      SCREENSHOT =
          AnalysedImage.analyse(
              ImageIO.read(
                  requireNonNull(AnalysedImageTest.class.getResourceAsStream("/screenshot.png"))));
      BLACK =
          AnalysedImage.analyse(
              ImageIO.read(
                  requireNonNull(AnalysedImageTest.class.getResourceAsStream("/black.png"))));
      WHITE =
          AnalysedImage.analyse(
              ImageIO.read(
                  requireNonNull(AnalysedImageTest.class.getResourceAsStream("/white.png"))));
      RED =
          AnalysedImage.analyse(
              ImageIO.read(
                  requireNonNull(AnalysedImageTest.class.getResourceAsStream("/red.png"))));
      GREEN =
          AnalysedImage.analyse(
              ImageIO.read(
                  requireNonNull(AnalysedImageTest.class.getResourceAsStream("/green.png"))));
      BLUE =
          AnalysedImage.analyse(
              ImageIO.read(
                  requireNonNull(AnalysedImageTest.class.getResourceAsStream("/blue.png"))));
      CYAN =
          AnalysedImage.analyse(
              ImageIO.read(
                  requireNonNull(AnalysedImageTest.class.getResourceAsStream("/cyan.png"))));
      MAGENTA =
          AnalysedImage.analyse(
              ImageIO.read(
                  requireNonNull(AnalysedImageTest.class.getResourceAsStream("/magenta.png"))));
      YELLOW =
          AnalysedImage.analyse(
              ImageIO.read(
                  requireNonNull(AnalysedImageTest.class.getResourceAsStream("/yellow.png"))));
    } catch (IOException e) {
      throw new RuntimeException("Failed to load test images", e);
    }
  }

  @Test
  void difference_screenshot() {
    assertThat(SCREENSHOT.difference(SCREENSHOT)).isZero();
  }

  @Test
  void difference_black_white() {
    assertThat(WHITE.difference(BLACK)).isEqualTo(1.0);
    assertThat(BLACK.difference(WHITE)).isEqualTo(1.0);
  }

  @Test
  void difference_primaries() {
    assertThat(RED.difference(RED)).isZero();
    assertThat(RED.difference(GREEN)).isEqualTo(6.0 / 9.0);
    assertThat(RED.difference(BLUE)).isEqualTo(6.0 / 9.0);
    assertThat(RED.difference(BLACK)).isEqualTo(3.0 / 9.0);
    assertThat(RED.difference(WHITE)).isEqualTo(6.0 / 9.0);
    assertThat(GREEN.difference(RED)).isEqualTo(6.0 / 9.0);
    assertThat(GREEN.difference(GREEN)).isZero();
    assertThat(GREEN.difference(BLUE)).isEqualTo(6.0 / 9.0);
    assertThat(GREEN.difference(BLACK)).isEqualTo(3.0 / 9.0);
    assertThat(GREEN.difference(WHITE)).isEqualTo(6.0 / 9.0);
    assertThat(BLUE.difference(RED)).isEqualTo(6.0 / 9.0);
    assertThat(BLUE.difference(GREEN)).isEqualTo(6.0 / 9.0);
    assertThat(BLUE.difference(BLUE)).isZero();
    assertThat(BLUE.difference(BLACK)).isEqualTo(3.0 / 9.0);
    assertThat(BLUE.difference(WHITE)).isEqualTo(6.0 / 9.0);
  }

  @Test
  void difference_secondaries() {
    assertThat(CYAN.difference(RED)).isOne();
    assertThat(CYAN.difference(GREEN)).isEqualTo(3.0 / 9.0);
    assertThat(CYAN.difference(BLUE)).isEqualTo(3.0 / 9.0);
    assertThat(CYAN.difference(BLACK)).isEqualTo(6.0 / 9.0);
    assertThat(CYAN.difference(WHITE)).isEqualTo(3.0 / 9.0);
    assertThat(MAGENTA.difference(RED)).isEqualTo(3.0 / 9.0);
    assertThat(MAGENTA.difference(GREEN)).isOne();
    assertThat(MAGENTA.difference(BLUE)).isEqualTo(3.0 / 9.0);
    assertThat(MAGENTA.difference(BLACK)).isEqualTo(6.0 / 9.0);
    assertThat(MAGENTA.difference(WHITE)).isEqualTo(3.0 / 9.0);
    assertThat(YELLOW.difference(RED)).isEqualTo(3.0 / 9.0);
    assertThat(YELLOW.difference(GREEN)).isEqualTo(3.0 / 9.0);
    assertThat(YELLOW.difference(BLUE)).isOne();
    assertThat(YELLOW.difference(BLACK)).isEqualTo(6.0 / 9.0);
    assertThat(YELLOW.difference(WHITE)).isEqualTo(3.0 / 9.0);
  }

  @Test
  void isMoreDifferentThan_screenshot() {
    assertThat(SCREENSHOT.isMoreDifferentThan(SCREENSHOT, 0.99999999)).isFalse();
  }

  @Test
  void isMoreDifferentThan_black_white() {
    assertThat(WHITE.isMoreDifferentThan(BLACK, 0.99999999)).isTrue();
    assertThat(BLACK.isMoreDifferentThan(WHITE, 0.99999999)).isTrue();
  }

  @Test
  void isMoreDifferentThan_primaries() {
    assertThat(RED.isMoreDifferentThan(RED, 0.99999999)).isFalse();
    assertThat(RED.isMoreDifferentThan(GREEN, 0.6)).isTrue();
    assertThat(RED.isMoreDifferentThan(BLUE, 0.6)).isTrue();
    assertThat(RED.isMoreDifferentThan(BLACK, 0.3)).isTrue();
    assertThat(RED.isMoreDifferentThan(WHITE, 0.6)).isTrue();
    assertThat(GREEN.isMoreDifferentThan(RED, 0.6)).isTrue();
    assertThat(GREEN.isMoreDifferentThan(GREEN, 0.99999999)).isFalse();
    assertThat(GREEN.isMoreDifferentThan(BLUE, 0.6)).isTrue();
    assertThat(GREEN.isMoreDifferentThan(BLACK, 0.3)).isTrue();
    assertThat(GREEN.isMoreDifferentThan(WHITE, 0.6)).isTrue();
    assertThat(BLUE.isMoreDifferentThan(RED, 0.6)).isTrue();
    assertThat(BLUE.isMoreDifferentThan(GREEN, 0.6)).isTrue();
    assertThat(BLUE.isMoreDifferentThan(BLUE, 0.99999999)).isFalse();
    assertThat(BLUE.isMoreDifferentThan(BLACK, 0.3)).isTrue();
    assertThat(BLUE.isMoreDifferentThan(WHITE, 0.6)).isTrue();
  }

  @Test
  void isMoreDifferentThan_secondaries() {
    assertThat(CYAN.isMoreDifferentThan(RED, 0.99999999)).isTrue();
    assertThat(CYAN.isMoreDifferentThan(GREEN, 0.3)).isTrue();
    assertThat(CYAN.isMoreDifferentThan(BLUE, 0.3)).isTrue();
    assertThat(CYAN.isMoreDifferentThan(BLACK, 0.6)).isTrue();
    assertThat(CYAN.isMoreDifferentThan(WHITE, 0.3)).isTrue();
    assertThat(MAGENTA.isMoreDifferentThan(RED, 0.3)).isTrue();
    assertThat(MAGENTA.isMoreDifferentThan(GREEN, 0.99999999)).isTrue();
    assertThat(MAGENTA.isMoreDifferentThan(BLUE, 0.3)).isTrue();
    assertThat(MAGENTA.isMoreDifferentThan(BLACK, 0.6)).isTrue();
    assertThat(MAGENTA.isMoreDifferentThan(WHITE, 0.3)).isTrue();
    assertThat(YELLOW.isMoreDifferentThan(RED, 0.3)).isTrue();
    assertThat(YELLOW.isMoreDifferentThan(GREEN, 0.3)).isTrue();
    assertThat(YELLOW.isMoreDifferentThan(BLUE, 0.99999999)).isTrue();
    assertThat(YELLOW.isMoreDifferentThan(BLACK, 0.6)).isTrue();
    assertThat(YELLOW.isMoreDifferentThan(WHITE, 0.3)).isTrue();
  }
}
