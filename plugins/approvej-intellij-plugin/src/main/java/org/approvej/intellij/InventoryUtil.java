package org.approvej.intellij;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Reads and queries the {@code .approvej/inventory.properties} file within the project. */
final class InventoryUtil {

  private static final Logger LOG = Logger.getInstance(InventoryUtil.class);

  private InventoryUtil() {}

  /**
   * Looks up the approved file in the inventory and returns the test reference, or {@code null} if
   * not found.
   */
  static @Nullable TestReference findTestReference(
      @NotNull VirtualFile approvedFile, @NotNull Project project) {
    VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
    if (projectDir == null) return null;

    Properties inventory = loadInventory(projectDir);
    String relativePath = VfsUtil.getRelativePath(approvedFile, projectDir);
    if (relativePath == null) return null;

    String testReference = inventory.getProperty(relativePath);
    return testReference != null ? parseTestReference(testReference) : null;
  }

  /**
   * Reverse lookup: finds all approved files for a given test method by searching inventory entries
   * for matching {@code ClassName#methodName} values.
   */
  static @NotNull List<VirtualFile> findApprovedFiles(
      @NotNull String className, @NotNull String methodName, @NotNull Project project) {
    VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
    if (projectDir == null) return List.of();

    Properties inventory = loadInventory(projectDir);
    String testReference = "%s#%s".formatted(className, methodName);
    return inventory.stringPropertyNames().stream()
        .filter(key -> testReference.equals(inventory.getProperty(key)))
        .map(projectDir::findFileByRelativePath)
        .filter(Objects::nonNull)
        .toList();
  }

  /**
   * Finds the test method that produces the given file by looking it up in the inventory and
   * resolving the class and method, or returns {@code null} if not found.
   */
  static @Nullable PsiMethod findTestMethod(@NotNull VirtualFile file, @NotNull Project project) {
    TestReference ref = findTestReference(file, project);
    if (ref == null) return null;
    PsiClass psiClass =
        JavaPsiFacade.getInstance(project)
            .findClass(ref.className(), GlobalSearchScope.projectScope(project));
    if (psiClass == null) return null;
    PsiMethod[] methods = psiClass.findMethodsByName(ref.methodName(), false);
    return methods.length > 0 ? methods[0] : null;
  }

  private static @NotNull Properties loadInventory(@NotNull VirtualFile projectDir) {
    Properties merged = new Properties();
    VfsUtil.visitChildrenRecursively(
        projectDir,
        new VirtualFileVisitor<Void>() {
          @Override
          public boolean visitFile(@NotNull VirtualFile file) {
            if (file.isDirectory()) {
              String name = file.getName();
              if (name.startsWith(".") && !name.equals(".approvej")) return false;
              if (name.equals("build") || name.equals("target") || name.equals("out")) return false;
              return true;
            }
            if (file.getName().equals("inventory.properties")
                && file.getParent() != null
                && ".approvej".equals(file.getParent().getName())) {
              loadAndMerge(file, projectDir, merged);
            }
            return true;
          }
        });
    return merged;
  }

  private static void loadAndMerge(
      @NotNull VirtualFile inventoryFile,
      @NotNull VirtualFile projectDir,
      @NotNull Properties merged) {
    VirtualFile moduleDir = inventoryFile.getParent().getParent();
    String prefix = VfsUtil.getRelativePath(moduleDir, projectDir);

    Properties props = new Properties();
    try (var reader =
        new InputStreamReader(inventoryFile.getInputStream(), StandardCharsets.ISO_8859_1)) {
      props.load(reader);
    } catch (IOException e) {
      LOG.warn("Failed to read inventory file: %s".formatted(inventoryFile.getPath()), e);
      return;
    }

    for (String key : props.stringPropertyNames()) {
      String projectRelativeKey = (prefix == null || prefix.isEmpty()) ? key : prefix + "/" + key;
      merged.setProperty(projectRelativeKey, props.getProperty(key));
    }
  }

  private static @Nullable TestReference parseTestReference(@NotNull String testReference) {
    int hashIndex = testReference.indexOf('#');
    if (hashIndex < 0) return null;
    return new TestReference(
        testReference.substring(0, hashIndex), testReference.substring(hashIndex + 1));
  }
}
