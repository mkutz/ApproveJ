package org.approvej.spring.fixtures;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class MockitoBeanBootTest {

  @MockitoBean FixtureService fixtureService;

  @Test
  void context() {
    assertThat(fixtureService).isNotNull();
  }
}
