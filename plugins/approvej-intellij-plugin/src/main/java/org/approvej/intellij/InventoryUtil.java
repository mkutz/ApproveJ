package org.approvej.intellij;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Reads and queries {@code .approvej/inventory.properties} files within the project. */
final class InventoryUtil {

  private static final Logger LOG = Logger.getInstance(InventoryUtil.class);

  private static final String INVENTORY_PATH = ".approvej/inventory.properties";

  private InventoryUtil() {}

  /** A reference to a test method: fully qualified class name and method name. */
  record TestReference(@NotNull String className, @NotNull String methodName) {}

  /** Finds all {@code .approvej/inventory.properties} files in the project. */
  static @NotNull List<VirtualFile> findInventoryFiles(@NotNull Project project) {
    VirtualFile baseDir = project.getBaseDir();
    if (baseDir == null) return List.of();

    List<VirtualFile> result = new ArrayList<>();
    collectInventoryFiles(baseDir, result);
    return result;
  }

  private static void collectInventoryFiles(
      @NotNull VirtualFile dir, @NotNull List<VirtualFile> result) {
    VirtualFile approvejDir = dir.findChild(".approvej");
    if (approvejDir != null && approvejDir.isDirectory()) {
      VirtualFile inventoryFile = approvejDir.findChild("inventory.properties");
      if (inventoryFile != null && !inventoryFile.isDirectory()) {
        result.add(inventoryFile);
      }
    }
    for (VirtualFile child : dir.getChildren()) {
      if (child.isDirectory()
          && !child.getName().startsWith(".")
          && !child.getName().equals("build")
          && !child.getName().equals("target")) {
        collectInventoryFiles(child, result);
      }
    }
  }

  /**
   * Looks up the approved file in inventory files and returns the test reference, or {@code null}
   * if not found.
   */
  static @Nullable TestReference findTestReference(
      @NotNull VirtualFile approvedFile, @NotNull Project project) {
    for (VirtualFile inventoryFile : findInventoryFiles(project)) {
      VirtualFile moduleRoot = inventoryFile.getParent().getParent();
      String relativePath = VfsUtil.getRelativePath(approvedFile, moduleRoot);
      if (relativePath == null) continue;

      Properties properties = loadProperties(inventoryFile);
      String testReference = properties.getProperty(relativePath);
      if (testReference != null) {
        return parseTestReference(testReference);
      }
    }
    return null;
  }

  /**
   * Reverse lookup: finds all approved files for a given test method by searching all inventory
   * entries for matching {@code ClassName#methodName} values.
   */
  static @NotNull List<VirtualFile> findApprovedFiles(
      @NotNull String className, @NotNull String methodName, @NotNull Project project) {
    String testReference = className + "#" + methodName;
    List<VirtualFile> result = new ArrayList<>();

    for (VirtualFile inventoryFile : findInventoryFiles(project)) {
      VirtualFile moduleRoot = inventoryFile.getParent().getParent();
      Properties properties = loadProperties(inventoryFile);
      for (String key : properties.stringPropertyNames()) {
        if (testReference.equals(properties.getProperty(key))) {
          VirtualFile approvedFile = moduleRoot.findFileByRelativePath(key);
          if (approvedFile != null) {
            result.add(approvedFile);
          }
        }
      }
    }
    return result;
  }

  private static @NotNull Properties loadProperties(@NotNull VirtualFile file) {
    Properties properties = new Properties();
    try (var reader = new InputStreamReader(file.getInputStream(), StandardCharsets.ISO_8859_1)) {
      properties.load(reader);
    } catch (IOException e) {
      LOG.warn("Failed to read inventory file: " + file.getPath(), e);
    }
    return properties;
  }

  private static @Nullable TestReference parseTestReference(@NotNull String testReference) {
    int hashIndex = testReference.indexOf('#');
    if (hashIndex < 0) return null;
    return new TestReference(
        testReference.substring(0, hashIndex), testReference.substring(hashIndex + 1));
  }
}
