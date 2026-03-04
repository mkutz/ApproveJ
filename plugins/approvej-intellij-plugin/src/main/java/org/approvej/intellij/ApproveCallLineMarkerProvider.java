package org.approvej.intellij;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UastUtils;

/**
 * Provides a gutter icon on {@code approve()...byFile()} chains that navigates to the approved
 * file.
 */
public final class ApproveCallLineMarkerProvider extends RelatedItemLineMarkerProvider {

  private static final Icon ICON = AllIcons.FileTypes.Text;

  @Override
  protected void collectNavigationMarkers(
      @NotNull PsiElement element,
      @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
    UCallExpression callExpression = ApproveCallUtil.asApproveCall(element);
    if (callExpression == null) return;

    if (!"byFile".equals(ApproveCallUtil.findTerminalCall(callExpression).lastMethodName())) return;

    UMethod uMethod = UastUtils.getParentOfType(callExpression, UMethod.class);
    if (uMethod == null) return;
    PsiMethod psiMethod = uMethod.getJavaPsi();
    PsiClass psiClass = psiMethod.getContainingClass();
    if (psiClass == null || psiClass.getQualifiedName() == null) return;

    String className = psiClass.getQualifiedName();
    String methodName = psiMethod.getName();
    Project project = element.getProject();

    List<VirtualFile> approvedFiles =
        InventoryUtil.findApprovedFiles(className, methodName, project);
    if (approvedFiles.isEmpty()) return;

    PsiManager psiManager = PsiManager.getInstance(project);
    List<PsiFile> targets =
        approvedFiles.stream().map(psiManager::findFile).filter(Objects::nonNull).toList();
    if (targets.isEmpty()) return;

    NavigationGutterIconBuilder<PsiElement> builder =
        NavigationGutterIconBuilder.create(ICON)
            .setTargets(targets)
            .setTooltipText(
                targets.size() == 1
                    ? "Navigate to %s".formatted(approvedFiles.getFirst().getName())
                    : "Navigate to approved file");
    result.add(builder.createLineMarkerInfo(element));
  }
}
