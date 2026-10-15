package org.approvej.spring.fixtures;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextHierarchy({
  @ContextConfiguration(classes = FixtureConfiguration.class),
  @ContextConfiguration(classes = FixtureApplication.class)
})
class HierarchyTest {

  @Test
  void context(@Autowired ApplicationContext context) {
    assertThat(context.getParent()).isNotNull();
  }
}
