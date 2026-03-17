package org.approvej.intellij

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ApprovedFileUtilTest {

  @Test
  fun `isApprovedFileName`() {
    assertThat(ApprovedFileUtil.isApprovedFileName("MyTest.byValue-approved.txt")).isTrue()
  }

  @Test
  fun `isApprovedFileName no extension`() {
    assertThat(ApprovedFileUtil.isApprovedFileName("MyTest.byValue-approved")).isTrue()
  }

  @Test
  fun `isApprovedFileName with affix`() {
    assertThat(ApprovedFileUtil.isApprovedFileName("MyTest.byValue-body-approved.json")).isTrue()
  }

  @Test
  fun `isApprovedFileName non-approved file`() {
    assertThat(ApprovedFileUtil.isApprovedFileName("MyTest.java")).isFalse()
  }

  @Test
  fun `isApprovedFileName received file`() {
    assertThat(ApprovedFileUtil.isApprovedFileName("MyTest.byValue-received.txt")).isFalse()
  }

  @Test
  fun `isApprovedFileName approved infix at start`() {
    assertThat(ApprovedFileUtil.isApprovedFileName("-approved.txt")).isFalse()
  }
}
