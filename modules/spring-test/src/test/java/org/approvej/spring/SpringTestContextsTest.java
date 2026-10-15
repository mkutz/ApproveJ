package org.approvej.spring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class SpringTestContextsTest {

  @Test
  void springTestContexts() {
    assertThat(SpringTestContexts.springTestContexts("org.approvej.spring.fixtures").contexts())
        .extracting(context -> context.testClasses().stream().map(Class::getSimpleName).toList())
        .containsExactly(
            List.of("ConfigTest"),
            List.of("CustomizedConfigTest"),
            List.of("DynamicPropertyBootTest"),
            List.of("HierarchyTest"),
            List.of("ImportBootTest"),
            List.of(
                "InheritedBootTest",
                "OtherPlainBootTest",
                "PlainBootTest",
                "NestedBootTest",
                "Inner"),
            List.of("MockitoBeanBootTest"),
            List.of("NoWebEnvironmentBootTest"),
            List.of("OtherCustomizedConfigTest"),
            List.of("ProfileBootTest"),
            List.of("PropertyBootTest"),
            List.of("PropertyMappingBootTest"));
  }

  @Test
  void springTestContexts_overlapping_base_packages() {
    assertThat(
            SpringTestContexts.springTestContexts(
                    "org.approvej.spring.fixtures.nested", "org.approvej.spring.fixtures")
                .contexts())
        .flatExtracting(SpringTestContext::testClasses)
        .hasSize(16)
        .doesNotHaveDuplicates();
  }

  @Test
  void springTestContexts_unknown_package() {
    assertThat(SpringTestContexts.springTestContexts("org.approvej.spring.unknown").contexts())
        .isEmpty();
  }
}
