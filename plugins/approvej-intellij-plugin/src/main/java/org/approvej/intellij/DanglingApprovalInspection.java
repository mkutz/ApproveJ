package org.approvej.intellij;

import com.intellij.codeInspection.AbstractBaseUastLocalInspectionTool;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UBlockExpression;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.ULambdaExpression;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UQualifiedReferenceExpression;
import org.jetbrains.uast.visitor.AbstractUastVisitor;

/**
 * Inspection that detects dangling {@code approve()} calls that are not concluded with a terminal
 * method ({@code by()}, {@code byFile()}, or {@code byValue()}).
 *
 * <p>Uses UAST to work across both Java and Kotlin.
 */
final class DanglingApprovalInspection extends AbstractBaseUastLocalInspectionTool {

  private static final String APPROVAL_BUILDER_CLASS = "org.approvej.ApprovalBuilder";
  private static final Set<String> TERMINAL_METHODS = Set.of("by", "byFile", "byValue");

  @Override
  public ProblemDescriptor @Nullable [] checkMethod(
      @NotNull UMethod method, @NotNull InspectionManager manager, boolean isOnTheFly) {
    var visitor = new DanglingApprovalVisitor();
    method.accept(visitor);
    List<ProblemDescriptor> problems =
        visitor.danglingApprovals.stream()
            .map(
                dangling ->
                    manager.createProblemDescriptor(
                        dangling.approveElement,
                        dangling.chainEndElement,
                        "Dangling approval: call by(), byFile(), or byValue() to conclude",
                        ProblemHighlightType.WARNING,
                        isOnTheFly,
                        new AppendTerminalMethodFix("byFile()"),
                        new AppendTerminalMethodFix("byValue(\"\")")))
            .toList();
    return problems.isEmpty() ? null : problems.toArray(ProblemDescriptor.EMPTY_ARRAY);
  }

  @Override
  public @NotNull String getStaticDescription() {
    return """
    Reports <code>approve()</code> calls on <code>ApprovalBuilder</code> that are not \
    concluded with a terminal method (<code>by()</code>, <code>byFile()</code>, or \
    <code>byValue()</code>). A missing terminal call means the approval is never actually \
    checked, causing a <code>DanglingApprovalError</code> at runtime.\
    """;
  }

  private record AppendTerminalMethodFix(String methodCall) implements LocalQuickFix {

    @Override
    public @NotNull String getName() {
      return "Conclude with .%s".formatted(methodCall);
    }

    @Override
    public @NotNull String getFamilyName() {
      return "Conclude dangling approval";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      PsiElement chainEnd = descriptor.getEndElement();
      if (chainEnd == null) return;

      Document document =
          PsiDocumentManager.getInstance(project).getDocument(chainEnd.getContainingFile());
      if (document == null) return;

      document.insertString(chainEnd.getTextRange().getEndOffset(), ".%s".formatted(methodCall));
      PsiDocumentManager.getInstance(project).commitDocument(document);
    }
  }

  private record DanglingApproval(PsiElement approveElement, PsiElement chainEndElement) {}

  private static final class DanglingApprovalVisitor extends AbstractUastVisitor {

    final List<DanglingApproval> danglingApprovals = new ArrayList<>();

    @Override
    public boolean visitCallExpression(@NotNull UCallExpression node) {
      if (!"approve".equals(node.getMethodName())) return false;

      PsiMethod method = node.resolve();
      if (method == null) return false;
      PsiClass containingClass = method.getContainingClass();
      if (containingClass == null
          || !APPROVAL_BUILDER_CLASS.equals(containingClass.getQualifiedName())) {
        return false;
      }

      UElement current = node;
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

      if (TERMINAL_METHODS.contains(lastMethodName)) return false;

      UElement chainParent = current.getUastParent();
      if (!(chainParent instanceof UBlockExpression || chainParent instanceof ULambdaExpression)) {
        return false;
      }

      PsiElement approveSourcePsi = node.getSourcePsi();
      PsiElement chainEndSourcePsi = current.getSourcePsi();
      if (approveSourcePsi == null) return false;
      if (chainEndSourcePsi == null) chainEndSourcePsi = approveSourcePsi;

      danglingApprovals.add(new DanglingApproval(approveSourcePsi, chainEndSourcePsi));
      return false;
    }
  }
}
