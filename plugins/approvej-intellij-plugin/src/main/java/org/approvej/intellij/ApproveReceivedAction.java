package org.approvej.intellij;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Action that approves a received file by copying its content to the approved file and deleting the
 * received file.
 */
public final class ApproveReceivedAction extends AnAction {

  @Override
  public void update(@NotNull AnActionEvent event) {
    event.getPresentation().setEnabledAndVisible(ReceivedFileUtil.isActionAvailable(event));
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    ReceivedFileUtil.withReceivedAndApproved(
        event,
        (received, approved) -> ReceivedFileUtil.approve(event.getProject(), received, approved));
  }
}
