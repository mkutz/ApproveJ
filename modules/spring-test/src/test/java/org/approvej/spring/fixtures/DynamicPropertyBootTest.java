package org.approvej.spring.fixtures;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class DynamicPropertyBootTest {

  @DynamicPropertySource
  static void fixtureProperties(DynamicPropertyRegistry registry) {
    registry.add("fixture.dynamic", () -> "true");
  }

  @Test
  void context(@Autowired ApplicationContext context) {
    assertThat(context.getEnvironment().getProperty("fixture.dynamic")).isEqualTo("true");
  }
}
