package org.approvej.spring.fixtures;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("fixture")
class ProfileBootTest {

  @Test
  void context(@Autowired ApplicationContext context) {
    assertThat(context.getEnvironment().getActiveProfiles()).containsExactly("fixture");
  }
}
