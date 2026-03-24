package org.approvej.intellij

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.getUParentForIdentifier

/**
 * Renames approved and received files when a test class or method is renamed via IntelliJ's
 * refactoring. The approved files are added to the same rename operation, making the rename atomic
 * and undoable.
 */
class ApproveJRenamePsiElementProcessor : RenamePsiElementProcessor() {

  override fun canProcessElement(element: PsiElement): Boolean {
    val simpleName = element::class.java.simpleName
    return element is PsiMethod ||
      element is PsiClass ||
      simpleName == "KtNamedFunction" ||
      simpleName == "KtClass"
  }

  override fun prepareRenaming(
    element: PsiElement,
    newName: String,
    allRenames: MutableMap<PsiElement, String>,
  ) {
    val effectiveNewName = stripBackticks(newName)
    val method = resolveMethod(element)
    if (method != null) {
      allRenames.putAll(prepareMethodRenaming(method, effectiveNewName))
      return
    }
    val clazz = resolveClass(element)
    if (clazz != null) {
      allRenames.putAll(prepareClassRenaming(clazz, effectiveNewName))
    }
  }

  companion object {
    /** Strips Kotlin backtick quoting from method names (e.g. `` `my test` `` → `my test`). */
    fun stripBackticks(name: String): String =
      if (name.length > 2) name.removeSurrounding("`") else name

    fun resolveMethod(element: PsiElement): PsiMethod? {
      if (element is PsiMethod) return element
      val uElement = toUElement(element)
      return (uElement as? UMethod)?.javaPsi
    }

    fun resolveClass(element: PsiElement): PsiClass? {
      if (element is PsiClass) return element
      val uElement = toUElement(element)
      return (uElement as? UClass)?.javaPsi
    }

    private fun toUElement(element: PsiElement): UElement? {
      if (element !is PsiNameIdentifierOwner) return null
      val nameIdentifier = element.nameIdentifier ?: return null
      return getUParentForIdentifier(nameIdentifier)
    }

    private fun prepareMethodRenaming(
      method: PsiMethod,
      newMethodName: String,
    ): Map<PsiElement, String> {
      val containingClass = method.containingClass ?: return emptyMap()
      val className = containingClass.qualifiedName ?: return emptyMap()
      val simpleClassName = containingClass.name ?: return emptyMap()
      val oldMethodName = method.name
      val project = method.project

      val approvedFiles = InventoryUtil.findApprovedFiles(className, oldMethodName, project)
      if (approvedFiles.isEmpty()) return emptyMap()

      val renames = mutableMapOf<PsiElement, String>()
      val psiManager = PsiManager.getInstance(project)

      fun addRename(file: VirtualFile) {
        val newFileName =
          computeNewFileName(file.name, simpleClassName, oldMethodName, newMethodName) ?: return
        val psiFile: PsiFile = psiManager.findFile(file) ?: return
        renames[psiFile] = newFileName
      }

      for (approvedFile in approvedFiles) {
        addRename(approvedFile)
        val receivedFile = ApprovedFileUtil.findReceivedFile(approvedFile)
        if (receivedFile != null) {
          addRename(receivedFile)
        }
      }
      return renames
    }

    private fun prepareClassRenaming(
      clazz: PsiClass,
      newClassName: String,
    ): Map<PsiElement, String> {
      val qualifiedName = clazz.qualifiedName ?: return emptyMap()
      val oldClassName = clazz.name ?: return emptyMap()
      val project = clazz.project
      val psiManager = PsiManager.getInstance(project)
      val renames = mutableMapOf<PsiElement, String>()
      val renamedDirectories = mutableSetOf<VirtualFile>()

      for (method in clazz.methods) {
        val methodName = method.name
        val approvedFiles = InventoryUtil.findApprovedFiles(qualifiedName, methodName, project)

        for (approvedFile in approvedFiles) {
          addClassFileRename(approvedFile, oldClassName, newClassName, psiManager, renames)
          val receivedFile = ApprovedFileUtil.findReceivedFile(approvedFile)
          if (receivedFile != null) {
            addClassFileRename(receivedFile, oldClassName, newClassName, psiManager, renames)
          }
          val parentDir = approvedFile.parent
          if (
            parentDir != null && parentDir.name == oldClassName && renamedDirectories.add(parentDir)
          ) {
            val psiDir = psiManager.findDirectory(parentDir)
            if (psiDir != null) renames[psiDir] = newClassName
          }
        }
      }
      return renames
    }

    private fun addClassFileRename(
      file: VirtualFile,
      oldClassName: String,
      newClassName: String,
      psiManager: PsiManager,
      renames: MutableMap<PsiElement, String>,
    ) {
      val filename = file.name
      if (!filename.startsWith("$oldClassName-")) return
      val newFileName = newClassName + filename.substring(oldClassName.length)
      val psiFile: PsiFile = psiManager.findFile(file) ?: return
      renames[psiFile] = newFileName
    }

    /**
     * Computes the new filename after a method rename. Returns `null` if the old method name cannot
     * be found in the filename.
     *
     * Handles two patterns:
     * - Next-to-test: `ClassName-oldMethod-approved.txt` → `ClassName-newMethod-approved.txt`
     * - Subdirectory: `oldMethod-approved.txt` → `newMethod-approved.txt`
     */
    fun computeNewFileName(
      filename: String,
      simpleClassName: String?,
      oldMethodName: String,
      newMethodName: String,
    ): String? {
      val classPrefix = "$simpleClassName-"
      if (filename.startsWith(classPrefix + oldMethodName)) {
        val methodStart = classPrefix.length
        val methodEnd = methodStart + oldMethodName.length
        if (
          methodEnd == filename.length || filename[methodEnd] == '-' || filename[methodEnd] == '.'
        ) {
          return filename.substring(0, methodStart) + newMethodName + filename.substring(methodEnd)
        }
      }
      if (filename.startsWith(oldMethodName)) {
        val methodEnd = oldMethodName.length
        if (
          methodEnd == filename.length || filename[methodEnd] == '-' || filename[methodEnd] == '.'
        ) {
          return newMethodName + filename.substring(methodEnd)
        }
      }
      return null
    }
  }
}
