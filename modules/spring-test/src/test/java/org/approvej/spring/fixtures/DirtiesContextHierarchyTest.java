package org.approvej.spring.fixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.HierarchyMode.CURRENT_LEVEL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextHierarchy({
  @ContextConfiguration(classes = FixtureConfiguration.class),
  @ContextConfiguration(classes = FixtureApplication.class)
})
class DirtiesContextHierarchyTest {

  @Test
  @DirtiesContext
  void context_exhaustive(@Autowired ApplicationContext context) {
    assertThat(context.getParent()).isNotNull();
  }

  @Test
  @DirtiesContext(hierarchyMode = CURRENT_LEVEL)
  void context_current_level(@Autowired ApplicationContext context) {
    assertThat(context.getParent()).isNotNull();
  }
}
