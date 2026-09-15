package io.kotest.provided

import io.kotest.core.config.AbstractProjectConfig
import org.approvej.kotest.ApprovejKotestExtension

/**
 * Kotest picks up this class by its fully-qualified name and applies the configured extensions to
 * every spec. Registering [ApprovejKotestExtension] here is exactly the setup users of the
 * approvej-kotest module need to perform.
 */
class ProjectConfig : AbstractProjectConfig() {
  override val extensions = listOf(ApprovejKotestExtension())
}
