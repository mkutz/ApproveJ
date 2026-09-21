package org.approvej.kotest

import io.kotest.core.spec.style.DescribeSpec
import org.approvej.approve.PathProviders.nextToTest
import org.assertj.core.api.Assertions.assertThat

/**
 * Leaves with the same name in different contexts must not collide. The enclosing context names
 * become part of the file name.
 */
class DescribeSpecApprovalTest :
  DescribeSpec({
    describe("first context") {
      it("uses a context-qualified file name") {
        val fileName = nextToTest().approvedPath().fileName.toString()
        assertThat(fileName)
          .contains("first context")
          .contains("uses a context-qualified file name")
      }
    }

    describe("second context") {
      it("uses a context-qualified file name") {
        val fileName = nextToTest().approvedPath().fileName.toString()
        assertThat(fileName)
          .contains("second context")
          .contains("uses a context-qualified file name")
      }
    }
  })
