package org.approvej.intellij

import com.intellij.openapi.actionSystem.ActionUpdateThread
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CompareTestActionTest {

  @Test
  fun `getActionUpdateThread`() {
    assertThat(CompareTestAction().actionUpdateThread).isEqualTo(ActionUpdateThread.BGT)
  }
}
