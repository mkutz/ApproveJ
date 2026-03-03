package org.approvej.intellij;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import java.util.function.Function;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Shows an info banner when a {@code .received} file is opened, with actions to compare or approve.
 */
public final class ReceivedFileEditorNotificationProvider
    implements EditorNotificationProvider, DumbAware {

  @Override
  public @Nullable Function<? super FileEditor, ? extends JComponent> collectNotificationData(
      @NotNull Project project, @NotNull VirtualFile file) {
    if (!ReceivedFileUtil.isReceivedFile(file)) return null;
    VirtualFile approvedFile = ReceivedFileUtil.findApprovedFile(file);
    return fileEditor -> {
      var panel = new EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info);
      if (approvedFile != null) {
        panel.setText("This is an ApproveJ received file that has not been approved yet.");
        panel.createActionLabel(
            "Compare with Approved", () -> ReceivedFileUtil.openDiff(project, file, approvedFile));
        panel.createActionLabel(
            "Approve", () -> ReceivedFileUtil.approve(project, file, approvedFile));
      } else {
        panel.setText(
            "This is an ApproveJ received file. No matching approved file was found nearby.");
      }
      return panel;
    };
  }
}
