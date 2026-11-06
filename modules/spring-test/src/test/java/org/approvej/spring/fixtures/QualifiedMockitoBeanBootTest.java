package org.approvej.spring.fixtures;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class QualifiedMockitoBeanBootTest {

  @MockitoBean(reset = MockReset.BEFORE)
  @Qualifier("fixtureService")
  FixtureService fixtureService;

  @Test
  void context() {
    assertThat(fixtureService).isNotNull();
  }
}
