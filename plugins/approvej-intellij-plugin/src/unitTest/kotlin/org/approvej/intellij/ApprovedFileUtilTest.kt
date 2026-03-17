package org.approvej.intellij

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ApprovedFileUtilTest {

  @Test
  fun isApprovedFileName() {
    assertThat(ApprovedFileUtil.isApprovedFileName("MyTest.byValue-approved.txt")).isTrue()
  }

  @Test
  fun isApprovedFileName_no_extension() {
    assertThat(ApprovedFileUtil.isApprovedFileName("MyTest.byValue-approved")).isTrue()
  }

  @Test
  fun isApprovedFileName_with_affix() {
    assertThat(ApprovedFileUtil.isApprovedFileName("MyTest.byValue-body-approved.json")).isTrue()
  }

  @Test
  fun isApprovedFileName_not_approved() {
    assertThat(ApprovedFileUtil.isApprovedFileName("MyTest.java")).isFalse()
  }

  @Test
  fun isApprovedFileName_received_file() {
    assertThat(ApprovedFileUtil.isApprovedFileName("MyTest.byValue-received.txt")).isFalse()
  }
}
