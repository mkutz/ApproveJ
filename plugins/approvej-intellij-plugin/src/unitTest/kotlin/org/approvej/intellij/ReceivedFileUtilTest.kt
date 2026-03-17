package org.approvej.intellij

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ReceivedFileUtilTest {

  @Test
  fun `isReceivedFileName`() {
    assertThat(ReceivedFileUtil.isReceivedFileName("MyTest.byValue-received.txt")).isTrue()
  }

  @Test
  fun `isReceivedFileName no extension`() {
    assertThat(ReceivedFileUtil.isReceivedFileName("MyTest.byValue-received")).isTrue()
  }

  @Test
  fun `isReceivedFileName with affix`() {
    assertThat(ReceivedFileUtil.isReceivedFileName("MyTest.byValue-body-received.json")).isTrue()
  }

  @Test
  fun `isReceivedFileName non-received file`() {
    assertThat(ReceivedFileUtil.isReceivedFileName("MyTest.java")).isFalse()
  }

  @Test
  fun `isReceivedFileName received infix at start`() {
    assertThat(ReceivedFileUtil.isReceivedFileName("-received.txt")).isFalse()
  }

  @Test
  fun `toApprovedFileName`() {
    assertThat(ReceivedFileUtil.toApprovedFileName("MyTest.byValue-received.txt"))
      .isEqualTo("MyTest.byValue-approved.txt")
  }

  @Test
  fun `toApprovedFileName no extension`() {
    assertThat(ReceivedFileUtil.toApprovedFileName("MyTest.byValue-received"))
      .isEqualTo("MyTest.byValue-approved")
  }

  @Test
  fun `toApprovedFileName with affix`() {
    assertThat(ReceivedFileUtil.toApprovedFileName("MyTest.byValue-body-received.json"))
      .isEqualTo("MyTest.byValue-body-approved.json")
  }

  @Test
  fun `toApprovedFileName non-received file`() {
    assertThat(ReceivedFileUtil.toApprovedFileName("MyTest.java")).isNull()
  }

  @Test
  fun `toBaseFileName`() {
    assertThat(ReceivedFileUtil.toBaseFileName("MyTest.byValue-received.txt"))
      .isEqualTo("MyTest.byValue.txt")
  }

  @Test
  fun `toBaseFileName no extension`() {
    assertThat(ReceivedFileUtil.toBaseFileName("MyTest.byValue-received"))
      .isEqualTo("MyTest.byValue")
  }

  @Test
  fun `toBaseFileName with affix`() {
    assertThat(ReceivedFileUtil.toBaseFileName("MyTest.byValue-body-received.json"))
      .isEqualTo("MyTest.byValue-body.json")
  }

  @Test
  fun `toBaseFileName non-received file`() {
    assertThat(ReceivedFileUtil.toBaseFileName("MyTest.java")).isNull()
  }

  @Test
  fun `approvedFileNameCandidates`() {
    assertThat(ReceivedFileUtil.approvedFileNameCandidates("MyTest.byValue-received.txt"))
      .containsExactly("MyTest.byValue-approved.txt", "MyTest.byValue.txt")
  }

  @Test
  fun `approvedFileNameCandidates no extension`() {
    assertThat(ReceivedFileUtil.approvedFileNameCandidates("MyTest.byValue-received"))
      .containsExactly("MyTest.byValue-approved", "MyTest.byValue")
  }

  @Test
  fun `approvedFileNameCandidates with affix`() {
    assertThat(ReceivedFileUtil.approvedFileNameCandidates("MyTest.byValue-body-received.json"))
      .containsExactly("MyTest.byValue-body-approved.json", "MyTest.byValue-body.json")
  }

  @Test
  fun `approvedFileNameCandidates non-received file`() {
    assertThat(ReceivedFileUtil.approvedFileNameCandidates("MyTest.java")).isEmpty()
  }
}
