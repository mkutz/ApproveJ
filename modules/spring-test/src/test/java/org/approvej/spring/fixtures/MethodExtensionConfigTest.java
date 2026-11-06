package org.approvej.spring.fixtures;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = FixtureConfiguration.class)
class MethodExtensionConfigTest {

  @Test
  @ExtendWith(SpringExtension.class)
  void context(@Autowired ApplicationContext context) {
    assertThat(context.getBean(FixtureConfiguration.class)).isNotNull();
  }
}
