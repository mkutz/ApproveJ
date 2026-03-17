package org.approvej.intellij

import com.intellij.openapi.actionSystem.ActionUpdateThread
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CompareReceivedWithApprovedActionTest {

  @Test
  fun getActionUpdateThread() {
    assertThat(CompareReceivedWithApprovedAction().actionUpdateThread)
      .isEqualTo(ActionUpdateThread.BGT)
  }
}
