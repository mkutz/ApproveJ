package org.approvej.intellij;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UastUtils;

/**
 * Renames approved and received files when a test class or method is renamed via IntelliJ's
 * refactoring. The approved files are added to the same rename operation, making the rename atomic
 * and undoable.
 */
public final class ApproveJRenamePsiElementProcessor extends RenamePsiElementProcessor {

  @Override
  public boolean canProcessElement(@NotNull PsiElement element) {
    String simpleName = element.getClass().getSimpleName();
    return element instanceof PsiMethod
        || element instanceof PsiClass
        || "KtNamedFunction".equals(simpleName)
        || "KtClass".equals(simpleName);
  }

  @Override
  public void prepareRenaming(
      @NotNull PsiElement element,
      @NotNull String newName,
      @NotNull Map<PsiElement, String> allRenames) {
    String effectiveNewName = stripBackticks(newName);
    PsiMethod method = resolveMethod(element);
    if (method != null) {
      allRenames.putAll(prepareMethodRenaming(method, effectiveNewName));
      return;
    }
    PsiClass clazz = resolveClass(element);
    if (clazz != null) {
      allRenames.putAll(prepareClassRenaming(clazz, effectiveNewName));
    }
  }

  /**
   * Strips Kotlin backtick quoting from method names (e.g. {@code `my test`} → {@code my test}).
   */
  static @NotNull String stripBackticks(@NotNull String name) {
    if (name.startsWith("`") && name.endsWith("`") && name.length() > 2) {
      return name.substring(1, name.length() - 1);
    }
    return name;
  }

  private static @Nullable PsiMethod resolveMethod(@NotNull PsiElement element) {
    if (element instanceof PsiMethod method) return method;
    UElement uElement = toUElement(element);
    if (uElement instanceof UMethod uMethod) return uMethod.getJavaPsi();
    return null;
  }

  private static @Nullable PsiClass resolveClass(@NotNull PsiElement element) {
    if (element instanceof PsiClass clazz) return clazz;
    UElement uElement = toUElement(element);
    if (uElement instanceof UClass uClass) return uClass.getJavaPsi();
    return null;
  }

  private static @Nullable UElement toUElement(@NotNull PsiElement element) {
    if (!(element instanceof PsiNameIdentifierOwner owner)) return null;
    PsiElement nameIdentifier = owner.getNameIdentifier();
    if (nameIdentifier == null) return null;
    return UastUtils.getUParentForIdentifier(nameIdentifier);
  }

  private static @NotNull Map<PsiElement, String> prepareMethodRenaming(
      @NotNull PsiMethod method, @NotNull String newMethodName) {
    PsiClass containingClass = method.getContainingClass();
    if (containingClass == null
        || containingClass.getQualifiedName() == null
        || containingClass.getName() == null) {
      return Map.of();
    }

    String className = containingClass.getQualifiedName();
    String oldMethodName = method.getName();
    String simpleClassName = containingClass.getName();
    Project project = method.getProject();

    List<VirtualFile> approvedFiles =
        InventoryUtil.findApprovedFiles(className, oldMethodName, project);
    if (approvedFiles.isEmpty()) return Map.of();

    VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
    Map<PsiElement, String> renames = new HashMap<>();
    Map<String, String> pathRenames = new HashMap<>();
    String oldTestReference = "%s#%s".formatted(className, oldMethodName);
    String newTestReference = "%s#%s".formatted(className, newMethodName);

    PsiManager psiManager = PsiManager.getInstance(project);
    for (VirtualFile approvedFile : approvedFiles) {
      String newFileName =
          computeNewFileName(approvedFile.getName(), simpleClassName, oldMethodName, newMethodName);
      if (newFileName != null) {
        PsiFile psiFile = psiManager.findFile(approvedFile);
        if (psiFile != null) {
          renames.put(psiFile, newFileName);
        }
        String relativePath = relativePath(projectDir, approvedFile);
        if (relativePath != null) {
          pathRenames.put(
              relativePath,
              relativePath.substring(0, relativePath.length() - approvedFile.getName().length())
                  + newFileName);
        }
      }

      VirtualFile receivedFile = ApprovedFileUtil.findReceivedFile(approvedFile);
      if (receivedFile != null) {
        String newReceivedName =
            computeNewFileName(
                receivedFile.getName(), simpleClassName, oldMethodName, newMethodName);
        if (newReceivedName != null) {
          PsiFile psiFile = psiManager.findFile(receivedFile);
          if (psiFile != null) {
            renames.put(psiFile, newReceivedName);
          }
        }
      }
    }

    InventoryUtil.updateEntries(project, pathRenames, Map.of(oldTestReference, newTestReference));
    return renames;
  }

  private record RenameResult(Map<PsiElement, String> renames, Map<String, String> pathRenames) {
    static final RenameResult EMPTY = new RenameResult(Map.of(), Map.of());
  }

  private static @NotNull Map<PsiElement, String> prepareClassRenaming(
      @NotNull PsiClass clazz, @NotNull String newClassName) {
    String qualifiedName = clazz.getQualifiedName();
    String oldClassName = clazz.getName();
    if (qualifiedName == null || oldClassName == null) return Map.of();

    Project project = clazz.getProject();
    PsiManager psiManager = PsiManager.getInstance(project);
    VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
    Map<PsiElement, String> renames = new HashMap<>();
    Set<VirtualFile> renamedDirectories = new HashSet<>();
    Map<String, String> pathRenames = new HashMap<>();
    Map<String, String> testReferenceRenames = new HashMap<>();

    String packageName = qualifiedName.substring(0, qualifiedName.length() - oldClassName.length());
    String newQualifiedName = packageName + newClassName;

    for (PsiMethod method : clazz.getMethods()) {
      String methodName = method.getName();
      List<VirtualFile> approvedFiles =
          InventoryUtil.findApprovedFiles(qualifiedName, methodName, project);

      String oldTestReference = "%s#%s".formatted(qualifiedName, methodName);
      String newTestReference = "%s#%s".formatted(newQualifiedName, methodName);
      if (!approvedFiles.isEmpty()) {
        testReferenceRenames.put(oldTestReference, newTestReference);
      }

      for (VirtualFile approvedFile : approvedFiles) {
        RenameResult fileResult =
            renameFileForClassChange(
                approvedFile, oldClassName, newClassName, psiManager, projectDir);
        renames.putAll(fileResult.renames());
        pathRenames.putAll(fileResult.pathRenames());

        VirtualFile receivedFile = ApprovedFileUtil.findReceivedFile(approvedFile);
        if (receivedFile != null) {
          RenameResult receivedResult =
              renameFileForClassChange(
                  receivedFile, oldClassName, newClassName, psiManager, projectDir);
          renames.putAll(receivedResult.renames());
          pathRenames.putAll(receivedResult.pathRenames());
        }

        RenameResult dirResult =
            renameDirForClassChange(
                approvedFile,
                oldClassName,
                newClassName,
                psiManager,
                renamedDirectories,
                projectDir);
        renames.putAll(dirResult.renames());
        pathRenames.putAll(dirResult.pathRenames());
      }
    }

    if (!pathRenames.isEmpty() || !testReferenceRenames.isEmpty()) {
      InventoryUtil.updateEntries(project, pathRenames, testReferenceRenames);
    }
    return renames;
  }

  private static @NotNull RenameResult renameFileForClassChange(
      @NotNull VirtualFile file,
      @NotNull String oldClassName,
      @NotNull String newClassName,
      @NotNull PsiManager psiManager,
      @Nullable VirtualFile projectDir) {
    String filename = file.getName();
    if (!filename.startsWith(oldClassName + "-")) return RenameResult.EMPTY;

    String newFileName = newClassName + filename.substring(oldClassName.length());
    Map<PsiElement, String> renames = new HashMap<>();
    PsiFile psiFile = psiManager.findFile(file);
    if (psiFile != null) {
      renames.put(psiFile, newFileName);
    }
    Map<String, String> pathRenames = new HashMap<>();
    String relativePath = relativePath(projectDir, file);
    if (relativePath != null) {
      pathRenames.put(
          relativePath,
          relativePath.substring(0, relativePath.length() - filename.length()) + newFileName);
    }
    return new RenameResult(renames, pathRenames);
  }

  private static @NotNull RenameResult renameDirForClassChange(
      @NotNull VirtualFile file,
      @NotNull String oldClassName,
      @NotNull String newClassName,
      @NotNull PsiManager psiManager,
      @NotNull Set<VirtualFile> renamedDirectories,
      @Nullable VirtualFile projectDir) {
    VirtualFile parentDir = file.getParent();
    if (parentDir == null
        || !parentDir.getName().equals(oldClassName)
        || !renamedDirectories.add(parentDir)) {
      return RenameResult.EMPTY;
    }
    Map<PsiElement, String> renames = new HashMap<>();
    PsiDirectory psiDir = psiManager.findDirectory(parentDir);
    if (psiDir != null) {
      renames.put(psiDir, newClassName);
    }
    Map<String, String> pathRenames = new HashMap<>();
    String oldDirPath = relativePath(projectDir, parentDir);
    if (oldDirPath != null) {
      String newDirPath =
          oldDirPath.substring(0, oldDirPath.length() - oldClassName.length()) + newClassName;
      for (VirtualFile child : parentDir.getChildren()) {
        pathRenames.put(oldDirPath + "/" + child.getName(), newDirPath + "/" + child.getName());
      }
    }
    return new RenameResult(renames, pathRenames);
  }

  private static @Nullable String relativePath(
      @Nullable VirtualFile projectDir, @NotNull VirtualFile file) {
    return projectDir != null ? VfsUtil.getRelativePath(file, projectDir) : null;
  }

  /**
   * Computes the new filename after a method rename. Returns {@code null} if the old method name
   * cannot be found in the filename.
   *
   * <p>Handles two patterns:
   *
   * <ul>
   *   <li>Next-to-test: {@code ClassName-oldMethod-approved.txt} → {@code
   *       ClassName-newMethod-approved.txt}
   *   <li>Subdirectory: {@code oldMethod-approved.txt} → {@code newMethod-approved.txt}
   * </ul>
   */
  static @Nullable String computeNewFileName(
      @NotNull String filename,
      String simpleClassName,
      @NotNull String oldMethodName,
      @NotNull String newMethodName) {
    String classPrefix = simpleClassName + "-";
    if (filename.startsWith(classPrefix + oldMethodName)) {
      int methodStart = classPrefix.length();
      int methodEnd = methodStart + oldMethodName.length();
      if (methodEnd == filename.length()
          || filename.charAt(methodEnd) == '-'
          || filename.charAt(methodEnd) == '.') {
        return filename.substring(0, methodStart) + newMethodName + filename.substring(methodEnd);
      }
    }
    if (filename.startsWith(oldMethodName)) {
      int methodEnd = oldMethodName.length();
      if (methodEnd == filename.length()
          || filename.charAt(methodEnd) == '-'
          || filename.charAt(methodEnd) == '.') {
        return newMethodName + filename.substring(methodEnd);
      }
    }
    return null;
  }
}
