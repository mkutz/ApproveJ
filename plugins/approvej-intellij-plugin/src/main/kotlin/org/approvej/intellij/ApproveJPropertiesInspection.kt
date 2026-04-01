package org.approvej.intellij

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.lang.properties.psi.Property
import com.intellij.psi.PsiFile

/**
 * Inspection that warns about unknown or deprecated property keys in `approvej.properties` files.
 *
 * Unknown keys are highlighted as warnings; deprecated keys are rendered with strikethrough.
 */
class ApproveJPropertiesInspection : LocalInspectionTool() {

  override fun checkFile(
    file: PsiFile,
    manager: InspectionManager,
    isOnTheFly: Boolean,
  ): Array<ProblemDescriptor>? {
    if (file.name != ApproveJProperties.FILE_NAME) return null
    val propertiesFile = file as? PropertiesFile ?: return null

    val problems = mutableListOf<ProblemDescriptor>()
    for (property in propertiesFile.properties) {
      val key = property.key ?: continue
      val keyElement = (property as? Property)?.firstChild ?: continue
      val definition = ApproveJProperties.BY_KEY[key]

      if (definition == null) {
        problems +=
          manager.createProblemDescriptor(
            keyElement,
            "Unknown ApproveJ property key '$key'",
            isOnTheFly,
            emptyArray(),
            ProblemHighlightType.WARNING,
          )
      } else if (definition.deprecated) {
        problems +=
          manager.createProblemDescriptor(
            keyElement,
            definition.deprecationMessage ?: "Deprecated property key '$key'",
            isOnTheFly,
            emptyArray(),
            ProblemHighlightType.LIKE_DEPRECATED,
          )
      }
    }
    return problems.toTypedArray().ifEmpty { null }
  }

  override fun getStaticDescription(): String =
    "Reports unknown or deprecated property keys in <code>approvej.properties</code> files." +
      " Unknown keys may indicate typos. Deprecated keys should be replaced with their" +
      " modern equivalents."
}
