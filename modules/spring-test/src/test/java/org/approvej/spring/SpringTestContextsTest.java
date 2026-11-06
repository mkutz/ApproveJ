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
            List.of("ConfigTest", "MethodExtensionConfigTest", "RecordConfigTest"),
            List.of("CustomizedConfigTest"),
            List.of(
                "DirtiesContextBootTest",
                "DirtiesContextMethodBootTest",
                "InheritedBootTest",
                "OtherPlainBootTest",
                "PlainBootTest",
                "OptionalDependencyBootTest",
                "NestedBootTest",
                "Inner"),
            List.of("DirtiesContextHierarchyTest", "HierarchyTest"),
            List.of("DynamicPropertyBootTest"),
            List.of("ImportBootTest"),
            List.of("MockitoBeanBootTest"),
            List.of("NoWebEnvironmentBootTest"),
            List.of("OtherCustomizedConfigTest"),
            List.of("ProfileBootTest"),
            List.of("PropertyBootTest"),
            List.of("PropertyMappingBootTest"),
            List.of("QualifiedMockitoBeanBootTest"),
            List.of("ReversedHierarchyTest"),
            List.of(),
            List.of("ShadowingBootTest"),
            List.of("ConcreteNestedTemplateTest", "Inner"));
  }

  @Test
  void springTestContexts_overlapping_base_packages() {
    assertThat(
            SpringTestContexts.springTestContexts(
                    "org.approvej.spring.fixtures.nested", "org.approvej.spring.fixtures")
                .contexts())
        .flatExtracting(SpringTestContext::testClasses)
        .hasSize(27)
        .doesNotHaveDuplicates();
  }

  @Test
  void springTestContexts_unknown_package() {
    assertThat(SpringTestContexts.springTestContexts("org.approvej.spring.unknown").contexts())
        .isEmpty();
  }
}
