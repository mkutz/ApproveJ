package org.approvej.intellij

import com.intellij.openapi.util.IconLoader

internal object ApproveJIcons {

  val APPROVEJ = IconLoader.getIcon("/icons/approvej.svg", ApproveJIcons::class.java)
  val APPROVED = IconLoader.getIcon("/icons/approved.svg", ApproveJIcons::class.java)
  val APPROVAL_PENDING = IconLoader.getIcon("/icons/approvalPending.svg", ApproveJIcons::class.java)
}
