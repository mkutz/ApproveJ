package org.approvej.spring.fixtures.nested;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class NestedBootTest {

  @Nested
  class Inner {

    @Test
    void context(@Autowired ApplicationContext context) {
      assertThat(context).isNotNull();
    }
  }
}
