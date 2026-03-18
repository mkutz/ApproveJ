package org.approvej.intellij

import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.refactoring.listeners.RefactoringElementListenerProvider

/**
 * Updates the `.approvej/inventory.properties` file after a test class or method is renamed. The
 * listener fires within the rename command, so the inventory update is part of the same undoable
 * operation.
 */
class ApproveJRefactoringListenerProvider : RefactoringElementListenerProvider {

  override fun getListener(element: PsiElement): RefactoringElementListener? {
    val method = ApproveJRenamePsiElementProcessor.resolveMethod(element)
    if (method != null) return methodRenameListener(method)
    val clazz = ApproveJRenamePsiElementProcessor.resolveClass(element)
    if (clazz != null) return classRenameListener(clazz)
    return null
  }

  companion object {
    private fun methodRenameListener(method: PsiMethod): RefactoringElementListener? {
      val containingClass = method.containingClass ?: return null
      val className = containingClass.qualifiedName ?: return null
      val simpleClassName = containingClass.name ?: return null
      val oldMethodName = method.name
      val project = method.project

      val approvedFiles = InventoryUtil.findApprovedFiles(className, oldMethodName, project)
      if (approvedFiles.isEmpty()) return null

      val projectDir = project.guessProjectDir()
      val oldPaths = captureRelativePaths(approvedFiles, projectDir)
      val oldTestReference = "$className#$oldMethodName"

      return object : RefactoringElementListener {
        override fun elementMoved(newElement: PsiElement) {}

        override fun elementRenamed(newElement: PsiElement) {
          val newMethodName =
            ApproveJRenamePsiElementProcessor.stripBackticks(
              (newElement as PsiNamedElement).name ?: return
            )

          val pathRenames = mutableMapOf<String, String>()
          for ((oldPath, oldFileName) in oldPaths) {
            val newFileName =
              ApproveJRenamePsiElementProcessor.computeNewFileName(
                oldFileName,
                simpleClassName,
                oldMethodName,
                newMethodName,
              )
            if (newFileName != null) {
              pathRenames[oldPath] =
                oldPath.substring(0, oldPath.length - oldFileName.length) + newFileName
            }
          }

          val newTestReference = "$className#$newMethodName"
          InventoryUtil.updateEntries(
            project,
            pathRenames,
            mapOf(oldTestReference to newTestReference),
          )
        }
      }
    }

    private fun classRenameListener(clazz: PsiClass): RefactoringElementListener? {
      val qualifiedName = clazz.qualifiedName ?: return null
      val oldClassName = clazz.name ?: return null
      val project = clazz.project
      val projectDir = project.guessProjectDir()

      val allOldPaths = mutableMapOf<String, String>()
      val oldTestReferences = mutableMapOf<String, String>()

      for (method in clazz.methods) {
        val methodName = method.name
        val approvedFiles = InventoryUtil.findApprovedFiles(qualifiedName, methodName, project)
        if (approvedFiles.isEmpty()) continue

        allOldPaths.putAll(captureRelativePaths(approvedFiles, projectDir))
        oldTestReferences["$qualifiedName#$methodName"] = methodName
      }

      if (allOldPaths.isEmpty()) return null

      return object : RefactoringElementListener {
        override fun elementMoved(newElement: PsiElement) {}

        override fun elementRenamed(newElement: PsiElement) {
          val newClassName = (newElement as PsiNamedElement).name ?: return
          val packageName = qualifiedName.substring(0, qualifiedName.length - oldClassName.length)
          val newQualifiedName = packageName + newClassName

          val pathRenames = mutableMapOf<String, String>()
          for ((oldPath, oldFileName) in allOldPaths) {
            if (oldFileName.startsWith("$oldClassName-")) {
              val newFileName = newClassName + oldFileName.substring(oldClassName.length)
              pathRenames[oldPath] =
                oldPath.substring(0, oldPath.length - oldFileName.length) + newFileName
            } else {
              val oldDirSegment = "/$oldClassName/"
              val newDirSegment = "/$newClassName/"
              val dirIndex = oldPath.indexOf(oldDirSegment)
              if (dirIndex >= 0) {
                pathRenames[oldPath] = oldPath.substring(0, dirIndex) + newDirSegment + oldFileName
              }
            }
          }

          val testReferenceRenames = mutableMapOf<String, String>()
          for ((oldRef, methodName) in oldTestReferences) {
            testReferenceRenames[oldRef] = "$newQualifiedName#$methodName"
          }

          InventoryUtil.updateEntries(project, pathRenames, testReferenceRenames)
        }
      }
    }

    private fun captureRelativePaths(
      files: List<VirtualFile>,
      projectDir: VirtualFile?,
    ): Map<String, String> {
      if (projectDir == null) return emptyMap()
      return buildMap {
        for (file in files) {
          val relativePath = VfsUtil.getRelativePath(file, projectDir)
          if (relativePath != null) {
            put(relativePath, file.name)
          }
        }
      }
    }
  }
}
