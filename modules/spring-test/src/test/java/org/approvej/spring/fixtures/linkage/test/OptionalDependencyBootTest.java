package org.approvej.spring.fixtures.linkage.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.approvej.spring.fixtures.linkage.OptionalDependency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class OptionalDependencyBootTest {

  @Test
  void context(@Autowired ApplicationContext context) {
    assertThat(context).isNotNull();
  }

  String use(OptionalDependency dependency) {
    return dependency.toString();
  }
}
