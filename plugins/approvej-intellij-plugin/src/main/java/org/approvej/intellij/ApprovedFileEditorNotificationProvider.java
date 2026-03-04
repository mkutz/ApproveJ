package org.approvej.intellij;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import java.util.function.Function;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Shows an info banner when an approved file is opened, with an action to jump to the test method
 * that produces it.
 */
public final class ApprovedFileEditorNotificationProvider implements EditorNotificationProvider {

  @Override
  public @Nullable Function<? super FileEditor, ? extends JComponent> collectNotificationData(
      @NotNull Project project, @NotNull VirtualFile file) {
    if (!ApprovedFileUtil.isApprovedFile(file)) return null;

    PsiMethod targetMethod = InventoryUtil.findTestMethod(file, project);
    VirtualFile receivedFile = ApprovedFileUtil.findReceivedFile(file);

    return fileEditor -> {
      var panel = new EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info);
      panel.setText("This is an ApproveJ approved file.");
      if (receivedFile != null) {
        panel.createActionLabel(
            "Compare with Received", () -> ReceivedFileUtil.openDiff(project, receivedFile, file));
      }
      if (targetMethod != null) {
        panel.createActionLabel("Jump to Test", () -> targetMethod.navigate(true));
      }
      return panel;
    };
  }
}
