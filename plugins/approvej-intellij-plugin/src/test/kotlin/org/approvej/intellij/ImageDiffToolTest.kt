package org.approvej.intellij

import com.intellij.diff.tools.binary.BinaryDiffTool
import com.intellij.diff.tools.simple.SimpleDiffTool
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ImageDiffToolTest {

  private val tool = ImageDiffTool()

  @Test
  fun name() {
    assertThat(tool.name).isEqualTo("ApproveJ Image Diff")
  }

  @Test
  fun suppressedTools() {
    assertThat(tool.suppressedTools)
      .containsExactly(SimpleDiffTool::class.java, BinaryDiffTool::class.java)
  }

  @Test
  fun isImageFile() {
    assertThat(isImageFile("photo.png")).isTrue()
    assertThat(isImageFile("photo.PNG")).isTrue()
    assertThat(isImageFile("photo.jpg")).isTrue()
    assertThat(isImageFile("photo.jpeg")).isTrue()
    assertThat(isImageFile("photo.gif")).isTrue()
    assertThat(isImageFile("photo.bmp")).isTrue()
    assertThat(isImageFile("photo.webp")).isTrue()
  }

  @Test
  fun isImageFile_non_image() {
    assertThat(isImageFile("document.txt")).isFalse()
    assertThat(isImageFile("code.java")).isFalse()
    assertThat(isImageFile("data.json")).isFalse()
    assertThat(isImageFile("noextension")).isFalse()
  }
}
