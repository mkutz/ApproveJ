package org.approvej.kotest

import io.kotest.core.spec.style.StringSpec
import org.approvej.ApprovalBuilder.approve

class StringSpecApprovalTest :
  StringSpec({ "approves a value by file" { approve("Some text").byFile() } })
