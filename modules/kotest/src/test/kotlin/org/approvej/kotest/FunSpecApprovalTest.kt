package org.approvej.kotest

import io.kotest.core.spec.style.FunSpec
import org.approvej.ApprovalBuilder.approve
import org.approvej.approve.PathProviders.nextToTest
import org.assertj.core.api.Assertions.assertThat

class FunSpecApprovalTest :
  FunSpec({
    test("approves a value by file") { approve("Some text").byFile() }

    test("derives the path from spec and test name") {
      val pathProvider = nextToTest()
      assertThat(pathProvider.approvedPath())
        .hasFileName("FunSpecApprovalTest-derives the path from spec and test name-approved.txt")
      assertThat(pathProvider.directory().toString().replace('\\', '/'))
        .endsWith("src/test/kotlin/org/approvej/kotest")
    }

    test("GET /article") {
      val pathProvider = nextToTest()
      assertThat(pathProvider.approvedPath())
        .hasFileName("FunSpecApprovalTest-GET _article-approved.txt")
    }
  })
