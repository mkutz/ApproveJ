package org.approvej.intellij

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ApproveJRenamePsiElementProcessorTest {

  @Nested
  inner class StripBackticks {

    @Test
    fun plain_name() {
      assertThat(ApproveJRenamePsiElementProcessor.stripBackticks("myMethod")).isEqualTo("myMethod")
    }

    @Test
    fun backticked_name() {
      assertThat(ApproveJRenamePsiElementProcessor.stripBackticks("`my test case`"))
        .isEqualTo("my test case")
    }

    @Test
    fun single_backtick() {
      assertThat(ApproveJRenamePsiElementProcessor.stripBackticks("`")).isEqualTo("`")
    }

    @Test
    fun empty_backticks() {
      assertThat(ApproveJRenamePsiElementProcessor.stripBackticks("``")).isEqualTo("``")
    }
  }

  @Nested
  inner class ComputeNewFileName {

    @Test
    fun next_to_test() {
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
    fun next_to_test_with_affix() {
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
    fun subdirectory() {
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
    fun no_match() {
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
