package org.approvej.intellij;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApprovedFileUtilTest {

  @Test
  void isApprovedFileName() {
    assertThat(ApprovedFileUtil.isApprovedFileName("MyTest.byValue-approved.txt")).isTrue();
  }

  @Test
  void isApprovedFileName_no_extension() {
    assertThat(ApprovedFileUtil.isApprovedFileName("MyTest.byValue-approved")).isTrue();
  }

  @Test
  void isApprovedFileName_with_affix() {
    assertThat(ApprovedFileUtil.isApprovedFileName("MyTest.byValue-body-approved.json")).isTrue();
  }

  @Test
  void isApprovedFileName_not_approved() {
    assertThat(ApprovedFileUtil.isApprovedFileName("MyTest.java")).isFalse();
  }

  @Test
  void isApprovedFileName_received_file() {
    assertThat(ApprovedFileUtil.isApprovedFileName("MyTest.byValue-received.txt")).isFalse();
  }
}
