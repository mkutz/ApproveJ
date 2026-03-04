package org.approvej.intellij;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/** Action that opens the diff viewer comparing a received file with its approved counterpart. */
public final class CompareReceivedWithApprovedAction extends AnAction {

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
    Project project = event.getProject();
    if (project == null) return;
    ReceivedFileUtil.withReceivedAndApproved(
        event, (received, approved) -> ReceivedFileUtil.openDiff(project, received, approved));
  }
}
