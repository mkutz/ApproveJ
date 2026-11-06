package org.approvej.spring.fixtures;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PlainTest {

  @Test
  void plain() {
    assertThat(PlainTest.class).isNotNull();
  }
}
