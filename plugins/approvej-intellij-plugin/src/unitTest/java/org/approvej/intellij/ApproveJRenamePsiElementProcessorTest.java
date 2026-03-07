package org.approvej.intellij;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ApproveJRenamePsiElementProcessorTest {

  @Nested
  class StripBackticks {

    @Test
    void plain_name() {
      assertThat(ApproveJRenamePsiElementProcessor.stripBackticks("myMethod"))
          .isEqualTo("myMethod");
    }

    @Test
    void backticked_name() {
      assertThat(ApproveJRenamePsiElementProcessor.stripBackticks("`my test case`"))
          .isEqualTo("my test case");
    }

    @Test
    void single_backtick() {
      assertThat(ApproveJRenamePsiElementProcessor.stripBackticks("`")).isEqualTo("`");
    }

    @Test
    void empty_backticks() {
      assertThat(ApproveJRenamePsiElementProcessor.stripBackticks("``")).isEqualTo("``");
    }
  }

  @Nested
  class ComputeNewFileName {

    @Test
    void next_to_test() {
      assertThat(
              ApproveJRenamePsiElementProcessor.computeNewFileName(
                  "MyTest-oldMethod-approved.txt", "MyTest", "oldMethod", "newMethod"))
          .isEqualTo("MyTest-newMethod-approved.txt");
    }

    @Test
    void next_to_test_with_affix() {
      assertThat(
              ApproveJRenamePsiElementProcessor.computeNewFileName(
                  "MyTest-oldMethod-jane-approved.txt", "MyTest", "oldMethod", "newMethod"))
          .isEqualTo("MyTest-newMethod-jane-approved.txt");
    }

    @Test
    void subdirectory() {
      assertThat(
              ApproveJRenamePsiElementProcessor.computeNewFileName(
                  "oldMethod-approved.txt", "MyTest", "oldMethod", "newMethod"))
          .isEqualTo("newMethod-approved.txt");
    }

    @Test
    void no_match() {
      assertThat(
              ApproveJRenamePsiElementProcessor.computeNewFileName(
                  "unrelated-file.txt", "MyTest", "oldMethod", "newMethod"))
          .isNull();
    }
  }
}
