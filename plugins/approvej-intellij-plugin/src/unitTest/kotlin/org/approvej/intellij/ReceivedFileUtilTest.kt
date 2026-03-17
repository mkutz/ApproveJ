package org.approvej.intellij

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ReceivedFileUtilTest {

  @Test
  fun isReceivedFileName() {
    assertThat(ReceivedFileUtil.isReceivedFileName("MyTest.byValue-received.txt")).isTrue()
  }

  @Test
  fun isReceivedFileName_not_received() {
    assertThat(ReceivedFileUtil.isReceivedFileName("MyTest.java")).isFalse()
  }

  @Test
  fun toApprovedFileName() {
    assertThat(ReceivedFileUtil.toApprovedFileName("MyTest.byValue-received.txt"))
      .isEqualTo("MyTest.byValue-approved.txt")
  }

  @Test
  fun toApprovedFileName_no_extension() {
    assertThat(ReceivedFileUtil.toApprovedFileName("MyTest.byValue-received"))
      .isEqualTo("MyTest.byValue-approved")
  }

  @Test
  fun toApprovedFileName_with_affix() {
    assertThat(ReceivedFileUtil.toApprovedFileName("MyTest.byValue-body-received.json"))
      .isEqualTo("MyTest.byValue-body-approved.json")
  }

  @Test
  fun toApprovedFileName_not_received() {
    assertThat(ReceivedFileUtil.toApprovedFileName("MyTest.java")).isNull()
  }

  @Test
  fun toBaseFileName() {
    assertThat(ReceivedFileUtil.toBaseFileName("MyTest.byValue-received.txt"))
      .isEqualTo("MyTest.byValue.txt")
  }

  @Test
  fun toBaseFileName_no_extension() {
    assertThat(ReceivedFileUtil.toBaseFileName("MyTest.byValue-received"))
      .isEqualTo("MyTest.byValue")
  }

  @Test
  fun toBaseFileName_with_affix() {
    assertThat(ReceivedFileUtil.toBaseFileName("MyTest.byValue-body-received.json"))
      .isEqualTo("MyTest.byValue-body.json")
  }

  @Test
  fun toBaseFileName_not_received() {
    assertThat(ReceivedFileUtil.toBaseFileName("MyTest.java")).isNull()
  }

  @Test
  fun approvedFileNameCandidates() {
    assertThat(ReceivedFileUtil.approvedFileNameCandidates("MyTest.byValue-received.txt"))
      .containsExactly("MyTest.byValue-approved.txt", "MyTest.byValue.txt")
  }

  @Test
  fun approvedFileNameCandidates_no_extension() {
    assertThat(ReceivedFileUtil.approvedFileNameCandidates("MyTest.byValue-received"))
      .containsExactly("MyTest.byValue-approved", "MyTest.byValue")
  }

  @Test
  fun approvedFileNameCandidates_with_affix() {
    assertThat(ReceivedFileUtil.approvedFileNameCandidates("MyTest.byValue-body-received.json"))
      .containsExactly("MyTest.byValue-body-approved.json", "MyTest.byValue-body.json")
  }

  @Test
  fun approvedFileNameCandidates_not_received() {
    assertThat(ReceivedFileUtil.approvedFileNameCandidates("MyTest.java")).isEmpty()
  }
}
