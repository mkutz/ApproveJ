package org.approvej.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import org.approvej.spring.fixtures.linkage.OptionalDependency;
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
            "org.approvej.spring.fixtures.DirtiesContextBootTest",
            "org.approvej.spring.fixtures.DirtiesContextHierarchyTest",
            "org.approvej.spring.fixtures.DirtiesContextMethodBootTest",
            "org.approvej.spring.fixtures.DynamicPropertyBootTest",
            "org.approvej.spring.fixtures.HierarchyTest",
            "org.approvej.spring.fixtures.ImportBootTest",
            "org.approvej.spring.fixtures.InheritedBootTest",
            "org.approvej.spring.fixtures.MethodExtensionConfigTest",
            "org.approvej.spring.fixtures.MockitoBeanBootTest",
            "org.approvej.spring.fixtures.NoWebEnvironmentBootTest",
            "org.approvej.spring.fixtures.OtherCustomizedConfigTest",
            "org.approvej.spring.fixtures.OtherPlainBootTest",
            "org.approvej.spring.fixtures.PlainBootTest",
            "org.approvej.spring.fixtures.ProfileBootTest",
            "org.approvej.spring.fixtures.PropertyBootTest",
            "org.approvej.spring.fixtures.PropertyMappingBootTest",
            "org.approvej.spring.fixtures.QualifiedMockitoBeanBootTest",
            "org.approvej.spring.fixtures.RecordConfigTest",
            "org.approvej.spring.fixtures.ReversedHierarchyTest",
            "org.approvej.spring.fixtures.inherited.sub.ShadowingBootTest",
            "org.approvej.spring.fixtures.linkage.test.OptionalDependencyBootTest",
            "org.approvej.spring.fixtures.nested.NestedBootTest",
            "org.approvej.spring.fixtures.nested.NestedBootTest$Inner",
            "org.approvej.spring.fixtures.template.ConcreteNestedTemplateTest",
            "org.approvej.spring.fixtures.template.NestedTemplate$Inner");
  }

  @Test
  void scan_unknown_package() {
    assertThat(scanner.scan("org.approvej.spring.unknown")).isEmpty();
  }

  @Test
  void scan_optional_dependency_missing() {
    SpringTestClassScanner missingDependencyScanner =
        new SpringTestClassScanner(new MissingOptionalDependencyClassLoader());

    assertThat(missingDependencyScanner.scan("org.approvej.spring.fixtures.linkage.production"))
        .isEmpty();
  }

  @Test
  void scan_optional_dependency_missing_spring_test() {
    SpringTestClassScanner missingDependencyScanner =
        new SpringTestClassScanner(new MissingOptionalDependencyClassLoader());

    assertThatThrownBy(
            () -> missingDependencyScanner.scan("org.approvej.spring.fixtures.linkage.test"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(
            "org.approvej.spring.fixtures.linkage.test.OptionalDependencyBootTest")
        .hasCauseInstanceOf(NoClassDefFoundError.class);
  }

  /**
   * Defines the classes of the linkage fixtures itself, but cannot find {@link OptionalDependency},
   * like an optional dependency that is missing at runtime.
   */
  private static final class MissingOptionalDependencyClassLoader extends ClassLoader {

    private static final String LINKAGE_PACKAGE = "org.approvej.spring.fixtures.linkage.";

    private MissingOptionalDependencyClassLoader() {
      super(SpringTestClassScannerTest.class.getClassLoader());
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      if (!name.startsWith(LINKAGE_PACKAGE)) {
        return super.loadClass(name, resolve);
      }
      if (name.equals(OptionalDependency.class.getName())) {
        throw new ClassNotFoundException(name);
      }
      synchronized (getClassLoadingLock(name)) {
        Class<?> loaded = findLoadedClass(name);
        if (loaded != null) {
          return loaded;
        }
        try (InputStream input =
            Objects.requireNonNull(
                getParent().getResourceAsStream(name.replace('.', '/') + ".class"))) {
          byte[] bytes = input.readAllBytes();
          return defineClass(name, bytes, 0, bytes.length);
        } catch (IOException e) {
          throw new ClassNotFoundException(name, e);
        }
      }
    }
  }
}
