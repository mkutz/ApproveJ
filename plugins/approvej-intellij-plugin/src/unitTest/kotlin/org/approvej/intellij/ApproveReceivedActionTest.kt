package org.approvej.intellij

import com.intellij.openapi.actionSystem.ActionUpdateThread
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ApproveReceivedActionTest {

  @Test
  fun getActionUpdateThread() {
    assertThat(ApproveReceivedAction().actionUpdateThread).isEqualTo(ActionUpdateThread.BGT)
  }
}
