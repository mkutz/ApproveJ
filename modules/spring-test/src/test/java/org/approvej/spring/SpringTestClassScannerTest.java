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
            "org.approvej.spring.fixtures.InheritedBootTest",
            "org.approvej.spring.fixtures.OtherPlainBootTest",
            "org.approvej.spring.fixtures.PlainBootTest",
            "org.approvej.spring.fixtures.ProfileBootTest",
            "org.approvej.spring.fixtures.PropertyBootTest",
            "org.approvej.spring.fixtures.nested.NestedBootTest",
            "org.approvej.spring.fixtures.nested.NestedBootTest$Inner");
  }

  @Test
  void scan_unknown_package() {
    assertThat(scanner.scan("org.approvej.spring.unknown")).isEmpty();
  }
}
