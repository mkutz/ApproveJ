package org.approvej.intellij;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import org.junit.jupiter.api.Test;

class ApproveReceivedActionTest {

  @Test
  void getActionUpdateThread() {
    assertThat(new ApproveReceivedAction().getActionUpdateThread())
        .isEqualTo(ActionUpdateThread.BGT);
  }
}
