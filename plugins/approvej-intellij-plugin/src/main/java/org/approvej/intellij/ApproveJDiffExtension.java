package org.approvej.intellij;

import com.intellij.diff.DiffContext;
import com.intellij.diff.DiffExtension;
import com.intellij.diff.FrameDiffTool;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.EditorNotificationPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

/**
 * Adds a notification panel with "Navigate to Test", "Approve", and "Reject" actions to the diff
 * viewer when comparing an ApproveJ received file with its approved counterpart.
 */
public final class ApproveJDiffExtension extends DiffExtension {

  @Override
  public void onViewerCreated(
      @NotNull FrameDiffTool.DiffViewer viewer,
      @NotNull DiffContext context,
      @NotNull DiffRequest request) {
    VirtualFile receivedFile = request.getUserData(ReceivedFileUtil.RECEIVED_FILE_KEY);
    VirtualFile approvedFile = request.getUserData(ReceivedFileUtil.APPROVED_FILE_KEY);
    if (receivedFile == null || approvedFile == null) return;

    Project project = context.getProject();
    if (project == null) return;

    PsiMethod testMethod = InventoryUtil.findTestMethod(approvedFile, project);

    JComponent panel = createNotificationPanel(project, receivedFile, approvedFile, testMethod);
    addToViewerNotifications(viewer, panel);
  }

  private static JComponent createNotificationPanel(
      @NotNull Project project,
      @NotNull VirtualFile receivedFile,
      @NotNull VirtualFile approvedFile,
      PsiMethod testMethod) {
    var panel = new EditorNotificationPanel(EditorNotificationPanel.Status.Info);
    panel.setText("ApproveJ: Received vs. Approved");
    if (testMethod != null) {
      panel.createActionLabel("Navigate to Test", () -> testMethod.navigate(true));
    }
    panel.createActionLabel(
        "Approve", () -> ReceivedFileUtil.approve(project, receivedFile, approvedFile));
    panel.createActionLabel("Reject", () -> ReceivedFileUtil.reject(project, receivedFile));
    return panel;
  }

  private static void addToViewerNotifications(
      @NotNull FrameDiffTool.DiffViewer viewer, @NotNull JComponent panel) {
    JComponent component = viewer.getComponent();
    if (component.getLayout() instanceof BorderLayout layout) {
      Component north = layout.getLayoutComponent(BorderLayout.NORTH);
      if (north instanceof JPanel northPanel) {
        northPanel.add(panel, 0);
      }
    }
  }
}
