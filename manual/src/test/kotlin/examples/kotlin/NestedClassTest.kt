package examples.kotlin

import org.approvej.ApprovalBuilder.approve
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class NestedClassTest {

  @Nested
  inner class InnerTest {

    @Test
    fun `approve by file`() {
      approve("approved from inner class").byFile()
    }

    @Nested
    inner class DoublyNestedTest {

      @Test
      fun `approve by file`() {
        approve("approved from doubly nested class").byFile()
      }
    }
  }
}
