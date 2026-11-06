package org.approvej.intellij

import com.intellij.openapi.actionSystem.ActionUpdateThread
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ApproveTestActionTest {

  @Test
  fun `getActionUpdateThread`() {
    assertThat(ApproveTestAction().actionUpdateThread).isEqualTo(ActionUpdateThread.BGT)
  }
}
