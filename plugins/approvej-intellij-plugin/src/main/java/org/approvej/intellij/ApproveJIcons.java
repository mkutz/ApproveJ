package org.approvej.intellij;

import com.intellij.openapi.util.IconLoader;
import javax.swing.Icon;

final class ApproveJIcons {

  static final Icon APPROVED = IconLoader.getIcon("/icons/approved.svg", ApproveJIcons.class);

  static final Icon APPROVAL_PENDING =
      IconLoader.getIcon("/icons/approvalPending.svg", ApproveJIcons.class);
}
