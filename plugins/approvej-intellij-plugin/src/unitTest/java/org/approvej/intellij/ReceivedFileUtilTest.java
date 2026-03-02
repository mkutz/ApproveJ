package org.approvej.intellij;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReceivedFileUtilTest {

  @Test
  void isReceivedFileName() {
    assertThat(ReceivedFileUtil.isReceivedFileName("MyTest.byValue-received.txt")).isTrue();
  }

  @Test
  void isReceivedFileName_not_received() {
    assertThat(ReceivedFileUtil.isReceivedFileName("MyTest.java")).isFalse();
  }

  @Test
  void isReceivedFileName_null() {
    assertThat(ReceivedFileUtil.isReceivedFileName(null)).isFalse();
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

  @Test
  void toApprovedFileName_null() {
    assertThat(ReceivedFileUtil.toApprovedFileName(null)).isNull();
  }

  @Test
  void toBaseFileName() {
    assertThat(ReceivedFileUtil.toBaseFileName("MyTest.byValue-received.txt"))
        .isEqualTo("MyTest.byValue.txt");
  }

  @Test
  void toBaseFileName_no_extension() {
    assertThat(ReceivedFileUtil.toBaseFileName("MyTest.byValue-received"))
        .isEqualTo("MyTest.byValue");
  }

  @Test
  void toBaseFileName_with_affix() {
    assertThat(ReceivedFileUtil.toBaseFileName("MyTest.byValue-body-received.json"))
        .isEqualTo("MyTest.byValue-body.json");
  }

  @Test
  void toBaseFileName_not_received() {
    assertThat(ReceivedFileUtil.toBaseFileName("MyTest.java")).isNull();
  }

  @Test
  void toBaseFileName_null() {
    assertThat(ReceivedFileUtil.toBaseFileName(null)).isNull();
  }

  @Test
  void approvedFileNameCandidates() {
    assertThat(ReceivedFileUtil.approvedFileNameCandidates("MyTest.byValue-received.txt"))
        .containsExactly("MyTest.byValue-approved.txt", "MyTest.byValue.txt");
  }

  @Test
  void approvedFileNameCandidates_no_extension() {
    assertThat(ReceivedFileUtil.approvedFileNameCandidates("MyTest.byValue-received"))
        .containsExactly("MyTest.byValue-approved", "MyTest.byValue");
  }

  @Test
  void approvedFileNameCandidates_with_affix() {
    assertThat(ReceivedFileUtil.approvedFileNameCandidates("MyTest.byValue-body-received.json"))
        .containsExactly("MyTest.byValue-body-approved.json", "MyTest.byValue-body.json");
  }
}
