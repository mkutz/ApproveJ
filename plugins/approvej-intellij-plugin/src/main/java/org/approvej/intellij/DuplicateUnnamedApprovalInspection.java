package org.approvej.intellij;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import com.intellij.codeInspection.AbstractBaseUastLocalInspectionTool;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UQualifiedReferenceExpression;
import org.jetbrains.uast.visitor.AbstractUastVisitor;

/**
 * Inspection that detects multiple {@code approve().byFile()} calls within the same test method
 * that resolve to the same approved file.
 *
 * <p>Without {@code .named()}, each no-arg {@code byFile()} uses the test method name, so the
 * second call silently overwrites the first.
 */
public final class DuplicateUnnamedApprovalInspection extends AbstractBaseUastLocalInspectionTool {

  private static final String UNNAMED_WARNING =
      "Duplicate unnamed approval: each subsequent byFile() overwrites the previous approved file."
          + " Use .named() to distinguish.";

  private static final String NAMED_WARNING_TEMPLATE =
      "Duplicate approval name '%s': each subsequent byFile() overwrites the previous approved"
          + " file. Use distinct names.";

  @Override
  public ProblemDescriptor @Nullable [] checkMethod(
      @NotNull UMethod method, @NotNull InspectionManager manager, boolean isOnTheFly) {
    var collector = new NoArgByFileCallCollector();
    method.accept(collector);

    Map<String, Long> countsByName =
        collector.calls.stream()
            .collect(
                groupingBy(
                    call -> call.effectiveName == null ? "" : call.effectiveName, counting()));

    List<ProblemDescriptor> problems =
        collector.calls.stream()
            .filter(
                call -> countsByName.get(call.effectiveName == null ? "" : call.effectiveName) > 1)
            .map(
                call -> {
                  String key = call.effectiveName == null ? "" : call.effectiveName;
                  boolean unnamed = key.isEmpty();
                  String message =
                      unnamed ? UNNAMED_WARNING : NAMED_WARNING_TEMPLATE.formatted(key);
                  LocalQuickFix[] fixes =
                      unnamed
                          ? new LocalQuickFix[] {new AddNamedCallFix()}
                          : LocalQuickFix.EMPTY_ARRAY;
                  return manager.createProblemDescriptor(
                      call.approveElement,
                      call.chainEndElement,
                      message,
                      ProblemHighlightType.WARNING,
                      isOnTheFly,
                      fixes);
                })
            .toList();

    return problems.isEmpty() ? null : problems.toArray(ProblemDescriptor.EMPTY_ARRAY);
  }

  @Override
  public @NotNull String getStaticDescription() {
    return """
    Reports multiple <code>approve().byFile()</code> calls within the same test method \
    that resolve to the same approved file. Without <code>.named()</code>, each no-arg \
    <code>byFile()</code> uses the test method name, so the second call silently overwrites \
    the first. Use <code>.named()</code> to give each approval a distinct name.\
    """;
  }

  private static int findByFileInsertOffset(@NotNull PsiElement chainEndElement) {
    String text = chainEndElement.getText();
    int dotByFile = text.lastIndexOf(".byFile(");
    if (dotByFile == -1) return -1;
    return chainEndElement.getTextRange().getStartOffset() + dotByFile;
  }

  private record NoArgByFileCall(
      PsiElement approveElement, PsiElement chainEndElement, @Nullable String effectiveName) {}

  private static final class NoArgByFileCallCollector extends AbstractUastVisitor {

    final List<NoArgByFileCall> calls = new ArrayList<>();

    @Override
    public boolean visitCallExpression(@NotNull UCallExpression node) {
      if (!ApproveCallUtil.isApproveCall(node)) return false;

      ApproveCallUtil.ChainWalkResult chain = ApproveCallUtil.findTerminalCall(node);
      if (!"byFile".equals(chain.lastMethodName())) return false;

      if (chain.chainEnd() instanceof UQualifiedReferenceExpression qualRef
          && qualRef.getSelector() instanceof UCallExpression terminalCall
          && !terminalCall.getValueArguments().isEmpty()) {
        return false;
      }

      PsiElement approveSourcePsi = node.getSourcePsi();
      PsiElement chainEndSourcePsi = chain.chainEnd().getSourcePsi();
      if (approveSourcePsi == null || chainEndSourcePsi == null) return false;

      String namedArg = ApproveCallUtil.findNamedArgument(node);
      if (namedArg == null && ApproveCallUtil.hasNamedCall(node)) return false;
      calls.add(new NoArgByFileCall(approveSourcePsi, chainEndSourcePsi, namedArg));
      return false;
    }
  }

  private record AddNamedCallFix() implements LocalQuickFix {

    @Override
    public @NotNull String getName() {
      return "Add .named(\"TODO\")";
    }

    @Override
    public @NotNull String getFamilyName() {
      return "Add .named(\"TODO\")";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      PsiElement element = descriptor.getEndElement();
      if (element == null) return;

      Document document =
          PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
      if (document == null) return;

      int offset = findByFileInsertOffset(element);
      if (offset < 0) return;

      String insertion = ".named(\"TODO\")";
      document.insertString(offset, insertion);
      PsiDocumentManager.getInstance(project).commitDocument(document);

      int todoStart = offset + ".named(\"".length();
      int todoEnd = todoStart + "TODO".length();
      Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
      if (editor != null && editor.getDocument() == document) {
        editor.getCaretModel().moveToOffset(todoStart);
        editor.getSelectionModel().setSelection(todoStart, todoEnd);
      }
    }
  }
}
