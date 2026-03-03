package org.approvej.intellij;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/** Action that opens the diff viewer comparing a received file with its approved counterpart. */
public final class CompareReceivedWithApprovedAction extends AnAction {

  @Override
  public void update(@NotNull AnActionEvent event) {
    VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
    event
        .getPresentation()
        .setEnabledAndVisible(
            ReceivedFileUtil.isReceivedFile(file)
                && ReceivedFileUtil.findApprovedFile(file) != null);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
    if (file == null || event.getProject() == null) return;
    VirtualFile approvedFile = ReceivedFileUtil.findApprovedFile(file);
    if (approvedFile == null) return;
    ReceivedFileUtil.openDiff(event.getProject(), file, approvedFile);
  }
}
