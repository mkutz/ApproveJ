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
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UQualifiedReferenceExpression;
import org.jetbrains.uast.UastUtils;

/**
 * Provides a gutter icon on {@code approve()...byFile()} chains that navigates to the approved
 * file.
 */
public final class ApproveCallLineMarkerProvider extends RelatedItemLineMarkerProvider {

  private static final String APPROVAL_BUILDER_CLASS = "org.approvej.ApprovalBuilder";
  private static final Icon ICON = AllIcons.FileTypes.Text;

  @Override
  protected void collectNavigationMarkers(
      @NotNull PsiElement element,
      @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
    UCallExpression callExpression = asApproveCall(element);
    if (callExpression == null) return;

    if (!isTerminatedByByFile(callExpression)) return;

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
        approvedFiles.stream().map(psiManager::findFile).filter(f -> f != null).toList();
    if (targets.isEmpty()) return;

    NavigationGutterIconBuilder<PsiElement> builder =
        NavigationGutterIconBuilder.create(ICON)
            .setTargets(targets)
            .setTooltipText(
                targets.size() == 1
                    ? "Navigate to " + approvedFiles.get(0).getName()
                    : "Navigate to approved file");
    result.add(builder.createLineMarkerInfo(element));
  }

  /**
   * Returns the UAST call expression if the given PSI element is the identifier of an {@code
   * approve()} call on {@code ApprovalBuilder}, or {@code null} otherwise.
   */
  private static UCallExpression asApproveCall(@NotNull PsiElement element) {
    // Only process leaf identifiers to avoid duplicate markers
    if (element.getFirstChild() != null) return null;

    UElement uElement = org.jetbrains.uast.UastUtils.getUParentForIdentifier(element);
    if (!(uElement instanceof UCallExpression callExpression)) return null;
    if (!"approve".equals(callExpression.getMethodName())) return null;

    PsiMethod method = callExpression.resolve();
    if (method == null) return null;
    PsiClass containingClass = method.getContainingClass();
    if (containingClass == null
        || !APPROVAL_BUILDER_CLASS.equals(containingClass.getQualifiedName())) {
      return null;
    }
    return callExpression;
  }

  /**
   * Walks the UAST parent chain from the approve() call to check if the chain ends with {@code
   * byFile()}.
   */
  private static boolean isTerminatedByByFile(@NotNull UCallExpression approveCall) {
    UElement current = approveCall;
    String lastMethodName = "approve";
    while (true) {
      UElement parent = current.getUastParent();
      if (parent instanceof UQualifiedReferenceExpression qualRef) {
        UExpression selector = qualRef.getSelector();
        if (selector instanceof UCallExpression selectorCall
            && selectorCall.getMethodName() != null) {
          lastMethodName = selectorCall.getMethodName();
          current = qualRef;
          continue;
        }
      }
      break;
    }
    return "byFile".equals(lastMethodName);
  }
}
