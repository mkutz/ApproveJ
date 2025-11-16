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
    assertThat(CYAN.difference(RED)).as("%s is complementary to %s", CYAN, RED).isOne();
    assertThat(CYAN.difference(GREEN)).isEqualTo(3.0 / 9);
    assertThat(CYAN.difference(BLUE)).isEqualTo(3.0 / 9);
    assertThat(CYAN.difference(BLACK)).isEqualTo(6.0 / 9);
    assertThat(CYAN.difference(WHITE)).isEqualTo(3.0 / 9);
    assertThat(MAGENTA.difference(RED)).isEqualTo(3.0 / 9);
    assertThat(MAGENTA.difference(GREEN)).as("%s is complementary to %s", MAGENTA, GREEN).isOne();
    assertThat(MAGENTA.difference(BLUE)).isEqualTo(3.0 / 9);
    assertThat(MAGENTA.difference(BLACK)).isEqualTo(6.0 / 9);
    assertThat(MAGENTA.difference(WHITE)).isEqualTo(3.0 / 9);
    assertThat(YELLOW.difference(RED)).isEqualTo(3.0 / 9);
    assertThat(YELLOW.difference(GREEN)).isEqualTo(3.0 / 9);
    assertThat(YELLOW.difference(BLUE)).as("%s is complementary to %s", YELLOW, BLUE).isOne();
    assertThat(YELLOW.difference(BLACK)).isEqualTo(6.0 / 9);
    assertThat(YELLOW.difference(WHITE)).isEqualTo(3.0 / 9);
  }
}
