package org.approvej.intellij;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.awt.RelativePoint;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UastUtils;

/**
 * Provides a gutter icon on {@code approve()...byFile()} chains that navigates to the approved
 * file. When a received file also exists, clicking the icon shows a popup with actions to compare,
 * navigate to received, or navigate to approved.
 */
public final class ApproveCallLineMarkerProvider extends LineMarkerProviderDescriptor {

  @Override
  public String getName() {
    return "ApproveJ approved file";
  }

  @Override
  public javax.swing.@Nullable Icon getIcon() {
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

    List<VirtualFile> approvedFiles =
        InventoryUtil.findApprovedFiles(className, methodName, project);
    String expectedName =
        namedArg != null
            ? "%s-%s-approved".formatted(methodName, namedArg)
            : "%s-approved".formatted(methodName);
    approvedFiles =
        approvedFiles.stream()
            .filter(f -> f.getNameWithoutExtension().equals(expectedName))
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
              psi -> "Received and approved files",
              (MouseEvent e, PsiElement elt) ->
                  showPopup(e, elt.getProject(), receivedFile, approvedFile),
              GutterIconRenderer.Alignment.LEFT,
              () -> "Received and approved files");
      result.add(markerInfo);
    }
  }

  private static void showPopup(
      @NotNull MouseEvent e,
      @NotNull Project project,
      @NotNull VirtualFile receivedFile,
      @NotNull VirtualFile approvedFile) {
    List<String> actions =
        List.of(
            "Compare Received and Approved",
            "Navigate to Received File",
            "Navigate to Approved File");
    var step =
        new BaseListPopupStep<>("ApproveJ", actions) {
          @Override
          public @Nullable PopupStep<?> onChosen(String selectedValue, boolean finalChoice) {
            return doFinalStep(
                () -> {
                  switch (selectedValue) {
                    case "Compare Received and Approved" ->
                        ReceivedFileUtil.openDiff(project, receivedFile, approvedFile);
                    case "Navigate to Received File" ->
                        new OpenFileDescriptor(project, receivedFile).navigate(true);
                    case "Navigate to Approved File" ->
                        new OpenFileDescriptor(project, approvedFile).navigate(true);
                    default -> {}
                  }
                });
          }
        };
    JBPopupFactory.getInstance().createListPopup(step).show(new RelativePoint(e));
  }
}
