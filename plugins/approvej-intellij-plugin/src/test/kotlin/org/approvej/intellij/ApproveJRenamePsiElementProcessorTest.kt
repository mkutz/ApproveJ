package org.approvej.intellij

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ApproveJRenamePsiElementProcessorTest {

  @Nested
  inner class StripBackticks {

    @Test
    fun `stripBackticks`() {
      assertThat(ApproveJRenamePsiElementProcessor.stripBackticks("myMethod")).isEqualTo("myMethod")
    }

    @Test
    fun `stripBackticks backticked name`() {
      assertThat(ApproveJRenamePsiElementProcessor.stripBackticks("`my test case`"))
        .isEqualTo("my test case")
    }

    @Test
    fun `stripBackticks single backtick`() {
      assertThat(ApproveJRenamePsiElementProcessor.stripBackticks("`")).isEqualTo("`")
    }

    @Test
    fun `stripBackticks empty backticks`() {
      assertThat(ApproveJRenamePsiElementProcessor.stripBackticks("``")).isEqualTo("``")
    }
  }

  @Nested
  inner class ComputeNewFileName {

    @Test
    fun `computeNewFileName`() {
      assertThat(
          ApproveJRenamePsiElementProcessor.computeNewFileName(
            "MyTest-oldMethod-approved.txt",
            "MyTest",
            "oldMethod",
            "newMethod",
          )
        )
        .isEqualTo("MyTest-newMethod-approved.txt")
    }

    @Test
    fun `computeNewFileName with affix`() {
      assertThat(
          ApproveJRenamePsiElementProcessor.computeNewFileName(
            "MyTest-oldMethod-jane-approved.txt",
            "MyTest",
            "oldMethod",
            "newMethod",
          )
        )
        .isEqualTo("MyTest-newMethod-jane-approved.txt")
    }

    @Test
    fun `computeNewFileName subdirectory`() {
      assertThat(
          ApproveJRenamePsiElementProcessor.computeNewFileName(
            "oldMethod-approved.txt",
            "MyTest",
            "oldMethod",
            "newMethod",
          )
        )
        .isEqualTo("newMethod-approved.txt")
    }

    @Test
    fun `computeNewFileName no match`() {
      assertThat(
          ApproveJRenamePsiElementProcessor.computeNewFileName(
            "unrelated-file.txt",
            "MyTest",
            "oldMethod",
            "newMethod",
          )
        )
        .isNull()
    }
  }
}
