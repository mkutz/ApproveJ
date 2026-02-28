package org.approvej.intellij

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Action that approves the current test by overwriting the `.approved` file with the `.actual`
 * file.
 *
 * This action is available in the diff viewer when an ApproveJ test fails, allowing the developer
 * to accept the new actual value as the approved baseline with a single click.
 */
class ApproveAction : AnAction() {

  override fun actionPerformed(event: AnActionEvent) {
    // TODO: Determine the .actual and .approved file paths from the context
    // TODO: Overwrite the .approved file with the .actual file content
    // TODO: Refresh the file system and notify the user
  }

  override fun update(event: AnActionEvent) {
    // TODO: Enable the action only when an .actual file is present in the context
    event.presentation.isEnabled = false
  }
}
