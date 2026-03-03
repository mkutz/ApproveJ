package org.approvej.intellij;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
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

    InventoryUtil.TestReference testRef = InventoryUtil.findTestReference(file, project);
    PsiMethod targetMethod = testRef != null ? resolveMethod(testRef, project) : null;

    return fileEditor -> {
      var panel = new EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info);
      panel.setText("This is an ApproveJ approved file.");
      if (targetMethod != null) {
        panel.createActionLabel("Jump to Test", () -> targetMethod.navigate(true));
      }
      return panel;
    };
  }

  private static @Nullable PsiMethod resolveMethod(
      @NotNull InventoryUtil.TestReference ref, @NotNull Project project) {
    PsiClass psiClass =
        JavaPsiFacade.getInstance(project)
            .findClass(ref.className(), GlobalSearchScope.projectScope(project));
    if (psiClass == null) return null;
    PsiMethod[] methods = psiClass.findMethodsByName(ref.methodName(), false);
    return methods.length > 0 ? methods[0] : null;
  }
}
