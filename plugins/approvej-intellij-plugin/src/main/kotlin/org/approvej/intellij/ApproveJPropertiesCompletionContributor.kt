package org.approvej.intellij

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.properties.psi.Property

/**
 * Provides autocompletion for property keys and values in `approvej.properties` files.
 *
 * In key position, all known property keys are offered. Deprecated keys are shown with
 * strikethrough. In value position, known values for the property key are offered.
 */
class ApproveJPropertiesCompletionContributor : CompletionContributor() {

  override fun fillCompletionVariants(
    parameters: CompletionParameters,
    result: CompletionResultSet,
  ) {
    val file = parameters.originalFile
    if (file.name != ApproveJProperties.FILE_NAME) return

    val position = parameters.position
    val property = position.parent as? Property

    if (property == null) {
      addKeyCompletions(result)
      return
    }

    if (isValuePosition(parameters, property)) {
      addValueCompletions(property, result)
    } else {
      addKeyCompletions(result)
    }
  }

  private fun isValuePosition(parameters: CompletionParameters, property: Property): Boolean {
    val keyElement = property.firstChild ?: return false
    return parameters.offset > keyElement.textRange.endOffset
  }

  private fun addKeyCompletions(result: CompletionResultSet) {
    for (definition in ApproveJProperties.ALL) {
      val element =
        LookupElementBuilder.create(definition.key).withStrikeoutness(definition.deprecated)
      result.addElement(element)
    }
  }

  private fun addValueCompletions(property: Property, result: CompletionResultSet) {
    val key = property.key ?: return
    val definition = ApproveJProperties.BY_KEY[key] ?: return
    val knownValues = definition.knownValues ?: return
    for (value in knownValues) {
      result.addElement(LookupElementBuilder.create(value))
    }
  }
}
