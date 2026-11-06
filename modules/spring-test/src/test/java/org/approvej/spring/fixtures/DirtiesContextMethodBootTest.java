package org.approvej.spring.fixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.MethodMode.BEFORE_METHOD;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
class DirtiesContextMethodBootTest {

  @Test
  void context(@Autowired ApplicationContext context) {
    assertThat(context).isNotNull();
  }

  @Test
  @DirtiesContext(methodMode = BEFORE_METHOD)
  void context_dirty(@Autowired ApplicationContext context) {
    assertThat(context).isNotNull();
  }
}
