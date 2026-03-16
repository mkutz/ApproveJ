package org.approvej.intellij;

import com.intellij.diff.DiffContext;
import com.intellij.diff.DiffExtension;
import com.intellij.diff.FrameDiffTool;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.EditorNotificationPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

/**
 * Adds a notification panel with "Navigate to Test", "Approve", and "Reject" actions to the diff
 * viewer when comparing an ApproveJ received file with its approved counterpart.
 */
final class ApproveJDiffExtension extends DiffExtension {

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

    JComponent panel =
        createNotificationPanel(project, receivedFile, approvedFile, testMethod, request);
    addToViewerNotifications(viewer, panel);
  }

  private static JComponent createNotificationPanel(
      @NotNull Project project,
      @NotNull VirtualFile receivedFile,
      @NotNull VirtualFile approvedFile,
      PsiMethod testMethod,
      @NotNull DiffRequest request) {
    var panel = new EditorNotificationPanel(EditorNotificationPanel.Status.Info);
    panel.setText("ApproveJ: Received vs. Approved");
    if (testMethod != null) {
      panel.createActionLabel("Navigate to Test", () -> testMethod.navigate(true));
    }
    panel.createActionLabel(
        "Approve",
        () -> {
          ReceivedFileUtil.approve(project, receivedFile, approvedFile);
          closeDiffAndNavigate(project, request, approvedFile);
        });
    panel.createActionLabel(
        "Reject",
        () -> {
          ReceivedFileUtil.reject(project, receivedFile);
          closeDiffAndNavigate(project, request, approvedFile);
        });
    return panel;
  }

  private static void closeDiffAndNavigate(
      @NotNull Project project, @NotNull DiffRequest request, @NotNull VirtualFile approvedFile) {
    var editorManager = FileEditorManager.getInstance(project);
    String diffTitle = request.getTitle();
    Arrays.stream(editorManager.getOpenFiles())
        .filter(file -> diffTitle != null && diffTitle.equals(file.getName()))
        .findFirst()
        .ifPresent(editorManager::closeFile);
    new OpenFileDescriptor(project, approvedFile).navigate(true);
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
