package examples.java;

import static org.approvej.ApprovalBuilder.approve;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NestedClassTest {

  @Nested
  class InnerTest {

    @Test
    void approve_by_file() {
      approve("approved from inner class").byFile();
    }

    @Nested
    class DoublyNestedTest {

      @Test
      void approve_by_file() {
        approve("approved from doubly nested class").byFile();
      }
    }
  }
}
