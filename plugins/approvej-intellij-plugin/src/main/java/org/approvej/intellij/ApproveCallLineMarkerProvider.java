package org.approvej.intellij;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UastUtils;

/**
 * Provides a gutter icon on {@code approve()...byFile()} chains that navigates to the approved
 * file. When a received file also exists, clicking the icon opens the diff viewer comparing the
 * received file with the approved file.
 */
public final class ApproveCallLineMarkerProvider extends LineMarkerProviderDescriptor {

  @Override
  public String getName() {
    return "ApproveJ approved file";
  }

  @Override
  public @NotNull Icon getIcon() {
    return ApproveJIcons.APPROVED;
  }

  @Override
  public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
    return null;
  }

  @Override
  public void collectSlowLineMarkers(
      @NotNull List<? extends PsiElement> elements,
      @NotNull Collection<? super LineMarkerInfo<?>> result) {
    for (PsiElement element : elements) {
      collectMarker(element, result);
    }
  }

  private void collectMarker(
      @NotNull PsiElement element, @NotNull Collection<? super LineMarkerInfo<?>> result) {
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

    String namedArg = ApproveCallUtil.findNamedArgument(callExpression);
    String suffix = namedArg != null ? namedArg + "-approved" : methodName + "-approved";

    List<VirtualFile> approvedFiles =
        InventoryUtil.findApprovedFiles(className, methodName, project).stream()
            .filter(
                approvedFile -> {
                  String name = approvedFile.getNameWithoutExtension();
                  int prefixLen = name.length() - suffix.length();
                  return name.endsWith(suffix)
                      && (prefixLen == 0 || name.charAt(prefixLen - 1) == '-');
                })
            .toList();
    if (approvedFiles.isEmpty()) return;

    PsiManager psiManager = PsiManager.getInstance(project);
    List<PsiFile> targets =
        approvedFiles.stream().map(psiManager::findFile).filter(Objects::nonNull).toList();
    if (targets.isEmpty()) return;

    VirtualFile firstApprovedWithReceived = null;
    VirtualFile matchingReceived = null;
    for (VirtualFile approved : approvedFiles) {
      VirtualFile received = ApprovedFileUtil.findReceivedFile(approved);
      if (received != null) {
        firstApprovedWithReceived = approved;
        matchingReceived = received;
        break;
      }
    }

    if (firstApprovedWithReceived == null) {
      NavigationGutterIconBuilder<PsiElement> builder =
          NavigationGutterIconBuilder.create(ApproveJIcons.APPROVED)
              .setTargets(targets)
              .setTooltipText(
                  targets.size() == 1
                      ? "Navigate to %s".formatted(approvedFiles.getFirst().getName())
                      : "Navigate to approved file");
      result.add(builder.createLineMarkerInfo(element));
    } else {
      VirtualFile approvedFile = firstApprovedWithReceived;
      VirtualFile receivedFile = matchingReceived;
      var markerInfo =
          new LineMarkerInfo<>(
              element,
              element.getTextRange(),
              ApproveJIcons.APPROVAL_PENDING,
              psi -> "Compare received with approved",
              (MouseEvent e, PsiElement elt) ->
                  ReceivedFileUtil.openDiff(elt.getProject(), receivedFile, approvedFile),
              GutterIconRenderer.Alignment.LEFT,
              () -> "Compare received with approved");
      result.add(markerInfo);
    }
  }
}
