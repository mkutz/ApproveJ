package org.approvej;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DanglingApprovalErrorTest {

  @Test
  void constructor() {
    var error = new DanglingApprovalError();

    assertThat(error)
        .hasMessageContaining("Dangling approval detected")
        .hasMessageContaining("by(), byFile(), or byValue()");
  }
}
