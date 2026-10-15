package org.approvej.spring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SpringTestClassScannerTest {

  private final SpringTestClassScanner scanner =
      new SpringTestClassScanner(SpringTestClassScannerTest.class.getClassLoader());

  @Test
  void scan() {
    assertThat(scanner.scan("org.approvej.spring.fixtures"))
        .extracting(Class::getName)
        .containsExactly(
            "org.approvej.spring.fixtures.ConfigTest",
            "org.approvej.spring.fixtures.CustomizedConfigTest",
            "org.approvej.spring.fixtures.DynamicPropertyBootTest",
            "org.approvej.spring.fixtures.HierarchyTest",
            "org.approvej.spring.fixtures.ImportBootTest",
            "org.approvej.spring.fixtures.InheritedBootTest",
            "org.approvej.spring.fixtures.MockitoBeanBootTest",
            "org.approvej.spring.fixtures.NoWebEnvironmentBootTest",
            "org.approvej.spring.fixtures.OtherCustomizedConfigTest",
            "org.approvej.spring.fixtures.OtherPlainBootTest",
            "org.approvej.spring.fixtures.PlainBootTest",
            "org.approvej.spring.fixtures.ProfileBootTest",
            "org.approvej.spring.fixtures.PropertyBootTest",
            "org.approvej.spring.fixtures.PropertyMappingBootTest",
            "org.approvej.spring.fixtures.nested.NestedBootTest",
            "org.approvej.spring.fixtures.nested.NestedBootTest$Inner");
  }

  @Test
  void scan_unknown_package() {
    assertThat(scanner.scan("org.approvej.spring.unknown")).isEmpty();
  }
}
