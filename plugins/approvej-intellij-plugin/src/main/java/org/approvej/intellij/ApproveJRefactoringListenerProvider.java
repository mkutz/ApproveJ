package org.approvej.intellij;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNamedElement;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.listeners.RefactoringElementListenerProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Updates the {@code .approvej/inventory.properties} file after a test class or method is renamed.
 * The listener fires within the rename command, so the inventory update is part of the same
 * undoable operation.
 */
public final class ApproveJRefactoringListenerProvider
    implements RefactoringElementListenerProvider {

  @Override
  public @Nullable RefactoringElementListener getListener(@NotNull PsiElement element) {
    PsiMethod method = ApproveJRenamePsiElementProcessor.resolveMethod(element);
    if (method != null) return methodRenameListener(method);
    PsiClass clazz = ApproveJRenamePsiElementProcessor.resolveClass(element);
    if (clazz != null) return classRenameListener(clazz);
    return null;
  }

  private static @Nullable RefactoringElementListener methodRenameListener(
      @NotNull PsiMethod method) {
    PsiClass containingClass = method.getContainingClass();
    if (containingClass == null
        || containingClass.getQualifiedName() == null
        || containingClass.getName() == null) {
      return null;
    }

    String className = containingClass.getQualifiedName();
    String simpleClassName = containingClass.getName();
    String oldMethodName = method.getName();
    Project project = method.getProject();

    List<VirtualFile> approvedFiles =
        InventoryUtil.findApprovedFiles(className, oldMethodName, project);
    if (approvedFiles.isEmpty()) return null;

    VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
    Map<String, String> oldPaths = captureRelativePaths(approvedFiles, projectDir);
    String oldTestReference = "%s#%s".formatted(className, oldMethodName);

    return new RefactoringElementListener() {
      @Override
      public void elementMoved(@NotNull PsiElement newElement) {}

      @Override
      public void elementRenamed(@NotNull PsiElement newElement) {
        String newMethodName =
            ApproveJRenamePsiElementProcessor.stripBackticks(
                ((PsiNamedElement) newElement).getName());

        Map<String, String> pathRenames = new HashMap<>();
        for (var entry : oldPaths.entrySet()) {
          String oldPath = entry.getKey();
          String oldFileName = entry.getValue();
          String newFileName =
              ApproveJRenamePsiElementProcessor.computeNewFileName(
                  oldFileName, simpleClassName, oldMethodName, newMethodName);
          if (newFileName != null) {
            pathRenames.put(
                oldPath,
                oldPath.substring(0, oldPath.length() - oldFileName.length()) + newFileName);
          }
        }

        String newTestReference = "%s#%s".formatted(className, newMethodName);
        InventoryUtil.updateEntries(
            project, pathRenames, Map.of(oldTestReference, newTestReference));
      }
    };
  }

  private static @Nullable RefactoringElementListener classRenameListener(@NotNull PsiClass clazz) {
    String qualifiedName = clazz.getQualifiedName();
    String oldClassName = clazz.getName();
    if (qualifiedName == null || oldClassName == null) return null;

    Project project = clazz.getProject();
    VirtualFile projectDir = ProjectUtil.guessProjectDir(project);

    Map<String, String> allOldPaths = new HashMap<>();
    Map<String, String> oldTestReferences = new HashMap<>();

    for (PsiMethod method : clazz.getMethods()) {
      String methodName = method.getName();
      List<VirtualFile> approvedFiles =
          InventoryUtil.findApprovedFiles(qualifiedName, methodName, project);
      if (approvedFiles.isEmpty()) continue;

      allOldPaths.putAll(captureRelativePaths(approvedFiles, projectDir));
      oldTestReferences.put("%s#%s".formatted(qualifiedName, methodName), methodName);
    }

    if (allOldPaths.isEmpty()) return null;

    return new RefactoringElementListener() {
      @Override
      public void elementMoved(@NotNull PsiElement newElement) {}

      @Override
      public void elementRenamed(@NotNull PsiElement newElement) {
        String newClassName = ((PsiNamedElement) newElement).getName();
        String packageName =
            qualifiedName.substring(0, qualifiedName.length() - oldClassName.length());
        String newQualifiedName = packageName + newClassName;

        Map<String, String> pathRenames = new HashMap<>();
        for (var entry : allOldPaths.entrySet()) {
          String oldPath = entry.getKey();
          String oldFileName = entry.getValue();

          if (oldFileName.startsWith(oldClassName + "-")) {
            String newFileName = newClassName + oldFileName.substring(oldClassName.length());
            pathRenames.put(
                oldPath,
                oldPath.substring(0, oldPath.length() - oldFileName.length()) + newFileName);
          } else {
            // Subdirectory pattern: path contains /OldClass/filename
            String oldDirSegment = "/" + oldClassName + "/";
            String newDirSegment = "/" + newClassName + "/";
            int dirIndex = oldPath.indexOf(oldDirSegment);
            if (dirIndex >= 0) {
              pathRenames.put(
                  oldPath, oldPath.substring(0, dirIndex) + newDirSegment + oldFileName);
            }
          }
        }

        Map<String, String> testReferenceRenames = new HashMap<>();
        for (var entry : oldTestReferences.entrySet()) {
          String oldRef = entry.getKey();
          String methodName = entry.getValue();
          testReferenceRenames.put(oldRef, "%s#%s".formatted(newQualifiedName, methodName));
        }

        InventoryUtil.updateEntries(project, pathRenames, testReferenceRenames);
      }
    };
  }

  private static @NotNull Map<String, String> captureRelativePaths(
      @NotNull List<VirtualFile> files, @Nullable VirtualFile projectDir) {
    Map<String, String> paths = new HashMap<>();
    if (projectDir == null) return paths;
    for (VirtualFile file : files) {
      String relativePath = VfsUtil.getRelativePath(file, projectDir);
      if (relativePath != null) {
        paths.put(relativePath, file.getName());
      }
    }
    return paths;
  }
}
