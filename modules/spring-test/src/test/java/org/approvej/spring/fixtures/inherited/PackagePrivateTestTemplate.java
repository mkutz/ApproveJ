package org.approvej.spring.fixtures.inherited;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(properties = "fixture=package-private-test")
public abstract class PackagePrivateTestTemplate {

  @Test
  void context(@Autowired ApplicationContext context) {
    assertThat(context).isNotNull();
  }
}
