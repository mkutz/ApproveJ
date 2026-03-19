package org.approvej.intellij

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.Properties

/** Reads and queries the `.approvej/inventory.properties` file within the project. */
internal object InventoryUtil {

  private val LOG = logger<InventoryUtil>()

  /**
   * Looks up the approved file in the inventory and returns the test reference, or `null` if not
   * found.
   */
  fun findTestReference(approvedFile: VirtualFile, project: Project): TestReference? {
    val projectDir = project.guessProjectDir() ?: return null
    val inventory = loadInventory(projectDir)
    val relativePath = VfsUtil.getRelativePath(approvedFile, projectDir) ?: return null
    val testReference = inventory.getProperty(relativePath) ?: return null
    return parseTestReference(testReference)
  }

  /**
   * Reverse lookup: finds all approved files for a given test method by searching inventory entries
   * for matching `ClassName#methodName` values.
   */
  fun findApprovedFiles(
    className: String,
    methodName: String,
    project: Project,
  ): List<VirtualFile> {
    val projectDir = project.guessProjectDir() ?: return emptyList()
    val inventory = loadInventory(projectDir)
    val testReference = "$className#$methodName"
    return inventory
      .stringPropertyNames()
      .filter { testReference == inventory.getProperty(it) }
      .sorted()
      .mapNotNull { projectDir.findFileByRelativePath(it) }
  }

  /**
   * Finds the test method that produces the given file by looking it up in the inventory and
   * resolving the class and method, or returns `null` if not found.
   */
  fun findTestMethod(file: VirtualFile, project: Project): PsiMethod? {
    val ref = findTestReference(file, project) ?: return null
    val psiClass =
      JavaPsiFacade.getInstance(project)
        .findClass(ref.className, GlobalSearchScope.projectScope(project)) ?: return null
    val methods = psiClass.findMethodsByName(ref.methodName, false)
    return methods.firstOrNull()
  }

  private fun forEachInventoryFile(projectDir: VirtualFile, action: (VirtualFile) -> Unit) {
    VfsUtil.visitChildrenRecursively(
      projectDir,
      object : VirtualFileVisitor<Unit>() {
        override fun visitFile(file: VirtualFile): Boolean {
          if (file.isDirectory) {
            val name = file.name
            if (name.startsWith(".") && name != ".approvej") return false
            if (name == "build" || name == "target" || name == "out") return false
            return true
          }
          if (
            file.name == "inventory.properties" &&
              file.parent != null &&
              file.parent.name == ".approvej"
          ) {
            action(file)
          }
          return true
        }
      },
    )
  }

  private fun loadInventory(projectDir: VirtualFile): Properties {
    val merged = Properties()
    forEachInventoryFile(projectDir) { file -> loadAndMerge(file, projectDir, merged) }
    return merged
  }

  private fun loadProperties(inventoryFile: VirtualFile): Properties? {
    val props = Properties()
    try {
      InputStreamReader(inventoryFile.inputStream, StandardCharsets.ISO_8859_1).use { reader ->
        props.load(reader)
      }
    } catch (e: java.io.IOException) {
      LOG.warn("Failed to read inventory file: ${inventoryFile.path}", e)
      return null
    }
    return props
  }

  private fun modulePrefix(inventoryFile: VirtualFile, projectDir: VirtualFile): String? {
    val inventoryDir = inventoryFile.parent ?: return null
    val moduleDir = inventoryDir.parent ?: return null
    return VfsUtil.getRelativePath(moduleDir, projectDir)
  }

  private fun loadAndMerge(
    inventoryFile: VirtualFile,
    projectDir: VirtualFile,
    merged: Properties,
  ) {
    val props = loadProperties(inventoryFile) ?: return
    val prefix = modulePrefix(inventoryFile, projectDir)
    for (key in props.stringPropertyNames()) {
      val projectRelativeKey = if (prefix.isNullOrEmpty()) key else "$prefix/$key"
      merged.setProperty(projectRelativeKey, props.getProperty(key))
    }
  }

  /**
   * Updates inventory entries when approved files are renamed. For each entry in [pathRenames], the
   * old project-relative path key is replaced with the new path. The test reference value is also
   * updated if it matches an entry in [testReferenceRenames].
   */
  fun updateEntries(
    project: Project,
    pathRenames: Map<String, String>,
    testReferenceRenames: Map<String, String>,
  ) {
    val projectDir = project.guessProjectDir() ?: return
    forEachInventoryFile(projectDir) { file ->
      updateInventoryFile(file, projectDir, pathRenames, testReferenceRenames)
    }
  }

  private fun updateInventoryFile(
    inventoryFile: VirtualFile,
    projectDir: VirtualFile,
    pathRenames: Map<String, String>,
    testReferenceRenames: Map<String, String>,
  ) {
    val properties = loadProperties(inventoryFile) ?: return
    val prefix = modulePrefix(inventoryFile, projectDir)
    var changed = false
    val updated = Properties()
    for (key in properties.stringPropertyNames()) {
      val value = properties.getProperty(key)
      val (newKey, newValue) = transformEntry(key, value, prefix, pathRenames, testReferenceRenames)
      updated.setProperty(newKey, newValue)
      if (newKey != key || newValue != value) {
        changed = true
      }
    }

    if (changed) {
      writeInventoryFile(inventoryFile, updated)
    }
  }

  private fun transformEntry(
    key: String,
    value: String,
    prefix: String?,
    pathRenames: Map<String, String>,
    testReferenceRenames: Map<String, String>,
  ): Pair<String, String> {
    val projectRelKey = if (prefix.isNullOrEmpty()) key else "$prefix/$key"
    val newProjectRelKey = pathRenames[projectRelKey]
    val newKey =
      if (newProjectRelKey != null) {
        if (prefix.isNullOrEmpty()) newProjectRelKey
        else newProjectRelKey.substring(prefix.length + 1)
      } else {
        key
      }
    val newValue = testReferenceRenames.getOrDefault(value, value)
    return newKey to newValue
  }

  private fun writeInventoryFile(inventoryFile: VirtualFile, properties: Properties) {
    ApplicationManager.getApplication().runWriteAction {
      try {
        OutputStreamWriter(
            inventoryFile.getOutputStream(InventoryUtil::class.java),
            StandardCharsets.ISO_8859_1,
          )
          .use { writer ->
            properties.store(
              writer,
              "ApproveJ Approved File Inventory (auto-generated, do not edit)",
            )
          }
      } catch (e: java.io.IOException) {
        LOG.warn("Failed to write inventory file: ${inventoryFile.path}", e)
      }
    }
  }

  private fun parseTestReference(testReference: String): TestReference? {
    val hashIndex = testReference.indexOf('#')
    if (hashIndex < 0) return null
    return TestReference(
      testReference.substring(0, hashIndex),
      testReference.substring(hashIndex + 1),
    )
  }
}
