package org.approvej.intellij

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class InventoryUtilTest {

  @Nested
  inner class NormalizeClassName {

    @Test
    fun `top level class unchanged`() {
      assertThat(InventoryUtil.normalizeClassName("com.example.MyTest"))
        .isEqualTo("com.example.MyTest")
    }

    @Test
    fun `nested class dollar replaced with dot`() {
      assertThat(InventoryUtil.normalizeClassName("com.example.OuterTest\$InnerTest"))
        .isEqualTo("com.example.OuterTest.InnerTest")
    }

    @Test
    fun `doubly nested class`() {
      assertThat(
          InventoryUtil.normalizeClassName("com.example.OuterTest\$InnerTest\$DoublyNestedTest")
        )
        .isEqualTo("com.example.OuterTest.InnerTest.DoublyNestedTest")
    }

    @Test
    fun `test reference with hash`() {
      assertThat(InventoryUtil.normalizeClassName("com.example.OuterTest\$InnerTest#myTest"))
        .isEqualTo("com.example.OuterTest.InnerTest#myTest")
    }
  }
}
