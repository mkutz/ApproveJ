package org.approvej.scrub;

import static org.approvej.scrub.Replacements.masking;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MaskingReplacementTest {

  @Test
  void apply_latin_letters() {
    assertThat(masking().apply("John Doe", 1)).isEqualTo("Aaaa Aaa");
  }

  @Test
  void apply_alphanumeric() {
    assertThat(masking().apply("B-AB 321", 1)).isEqualTo("A-AA 111");
  }

  @Test
  void apply_japanese_letters() {
    assertThat(masking().apply("遙斗", 1)).isEqualTo("aa");
  }

  @Test
  void apply_cyrillic_letters() {
    assertThat(masking().apply("В’ячесла́в", 1)).isEqualTo("A’aaaaaaa");
  }

  @Test
  void apply_arabic_alphanumeric() {
    assertThat(masking().apply("ایران ۲۴ ب ۴۲۳", 1)).isEqualTo("aaaaa 11 a 111");
  }
}
