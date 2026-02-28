package org.approvej.intellij;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReceivedFileUtilTest {

  @Test
  void isReceivedFileName() {
    assertThat(ReceivedFileUtil.isReceivedFileName("MyTest.byValue-received.txt")).isTrue();
  }

  @Test
  void isReceivedFileName_no_extension() {
    assertThat(ReceivedFileUtil.isReceivedFileName("MyTest.byValue-received")).isTrue();
  }

  @Test
  void isReceivedFileName_with_affix() {
    assertThat(ReceivedFileUtil.isReceivedFileName("MyTest.byValue-body-received.json")).isTrue();
  }

  @Test
  void isReceivedFileName_approved_file() {
    assertThat(ReceivedFileUtil.isReceivedFileName("MyTest.byValue-approved.txt")).isFalse();
  }

  @Test
  void isReceivedFileName_regular_file() {
    assertThat(ReceivedFileUtil.isReceivedFileName("MyTest.java")).isFalse();
  }

  @Test
  void isReceivedFileName_null() {
    assertThat(ReceivedFileUtil.isReceivedFileName(null)).isFalse();
  }

  @Test
  void isReceivedFileName_received_in_middle() {
    assertThat(ReceivedFileUtil.isReceivedFileName("received-data.txt")).isFalse();
  }

  @Test
  void toApprovedFileName() {
    assertThat(ReceivedFileUtil.toApprovedFileName("MyTest.byValue-received.txt"))
        .isEqualTo("MyTest.byValue-approved.txt");
  }

  @Test
  void toApprovedFileName_no_extension() {
    assertThat(ReceivedFileUtil.toApprovedFileName("MyTest.byValue-received"))
        .isEqualTo("MyTest.byValue-approved");
  }

  @Test
  void toApprovedFileName_with_affix() {
    assertThat(ReceivedFileUtil.toApprovedFileName("MyTest.byValue-body-received.json"))
        .isEqualTo("MyTest.byValue-body-approved.json");
  }

  @Test
  void toApprovedFileName_not_received() {
    assertThat(ReceivedFileUtil.toApprovedFileName("MyTest.java")).isNull();
  }
}
