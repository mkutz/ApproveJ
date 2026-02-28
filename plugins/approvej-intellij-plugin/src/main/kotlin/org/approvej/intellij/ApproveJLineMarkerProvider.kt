package org.approvej.intellij

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.psi.PsiElement

/**
 * Provides gutter icons next to ApproveJ test methods.
 *
 * Identifies methods that call [org.approvej.ApprovalBuilder] and shows an icon in the gutter that
 * allows quick navigation to the corresponding `.approved` file.
 */
class ApproveJLineMarkerProvider : RelatedItemLineMarkerProvider() {

  override fun collectNavigationMarkers(
    element: PsiElement,
    result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
  ) {
    // TODO: Detect method calls to ApprovalBuilder (e.g. `.byFile()`)
    // TODO: Find the corresponding .approved file in the project
    // TODO: Add a gutter icon that navigates to the .approved file
  }
}
