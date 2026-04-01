package org.approvej.intellij

import com.intellij.lang.properties.codeInspection.unused.ImplicitPropertyUsageProvider
import com.intellij.lang.properties.psi.Property

/**
 * Marks all properties in `approvej.properties` files as implicitly used so the built-in "Unused
 * property" inspection does not flag them.
 */
class ApproveJImplicitPropertyUsageProvider : ImplicitPropertyUsageProvider {

  override fun isUsed(property: Property): Boolean =
    property.containingFile?.name == ApproveJProperties.FILE_NAME
}
