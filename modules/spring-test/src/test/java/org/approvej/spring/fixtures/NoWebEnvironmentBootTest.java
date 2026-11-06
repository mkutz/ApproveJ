package org.approvej.spring.fixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(webEnvironment = NONE)
class NoWebEnvironmentBootTest {

  @Test
  void context(@Autowired ApplicationContext context) {
    assertThat(context).isNotNull();
  }
}
