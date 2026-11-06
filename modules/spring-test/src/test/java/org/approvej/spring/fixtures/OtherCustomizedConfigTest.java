package org.approvej.spring.fixtures;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(FixtureConfiguration.class)
@FixtureCustomization("b")
class OtherCustomizedConfigTest {

  @Test
  void context(@Autowired ApplicationContext context) {
    assertThat(context).isNotNull();
  }
}
