package org.approvej.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.HashMap;
import java.util.Map;
import org.approvej.print.MultiLineStringPrintFormat;
import org.approvej.print.PrintFormat;
import org.approvej.print.SingleLineStringPrintFormat;
import org.approvej.review.FileReviewer;
import org.junit.jupiter.api.Test;

class RegistryTest {

  @Test
  void resolve_printFormat_singleLineString() {
    PrintFormat<?> format = Registry.resolve("singleLineString", PrintFormat.class);

    assertThat(format).isInstanceOf(SingleLineStringPrintFormat.class);
  }

  @Test
  void resolve_printFormat_multiLineString() {
    PrintFormat<?> format = Registry.resolve("multiLineString", PrintFormat.class);

    assertThat(format).isInstanceOf(MultiLineStringPrintFormat.class);
  }

  @Test
  void resolve_printFormat_byClassName() {
    PrintFormat<?> format =
        Registry.resolve(SingleLineStringPrintFormat.class.getName(), PrintFormat.class);

    assertThat(format).isInstanceOf(SingleLineStringPrintFormat.class);
  }

  @Test
  void resolve_fileReviewer_none() {
    FileReviewer reviewer = Registry.resolve("none", FileReviewer.class);

    assertThat(reviewer).isNotNull();
  }

  @Test
  void resolve_fileReviewer_automatic() {
    FileReviewer reviewer = Registry.resolve("automatic", FileReviewer.class);

    assertThat(reviewer).isNotNull();
  }

  @Test
  void resolve_unknownAlias_fallsBackToClassName() {
    assertThatExceptionOfType(ConfigurationError.class)
        .isThrownBy(() -> Registry.resolve("unknownAlias", PrintFormat.class))
        .withMessageContaining("Failed to create")
        .withMessageContaining("unknownAlias");
  }

  @Test
  void resolve_invalidClassName_throwsError() {
    assertThatExceptionOfType(ConfigurationError.class)
        .isThrownBy(() -> Registry.resolve("org.nonexistent.Class", PrintFormat.class))
        .withMessageContaining("Failed to create")
        .withMessageContaining("org.nonexistent.Class");
  }

  @Test
  void registerProvider_duplicateAlias_throwsError() {
    Map<Class<?>, Map<String, Provider<?>>> registry = new HashMap<>();

    Provider<PrintFormat<?>> provider1 = new TestProvider("duplicate");
    Provider<PrintFormat<?>> provider2 = new TestProvider("duplicate");

    Registry.registerProvider(registry, provider1);

    assertThatExceptionOfType(ConfigurationError.class)
        .isThrownBy(() -> Registry.registerProvider(registry, provider2))
        .withMessageContaining("Duplicate provider alias 'duplicate' for type PrintFormat")
        .withMessageContaining("Remove one of the conflicting dependencies");
  }

  @Test
  void registerProvider_differentAliases_succeeds() {
    Map<Class<?>, Map<String, Provider<?>>> registry = new HashMap<>();

    Provider<PrintFormat<?>> provider1 = new TestProvider("alias1");
    Provider<PrintFormat<?>> provider2 = new TestProvider("alias2");

    Registry.registerProvider(registry, provider1);
    Registry.registerProvider(registry, provider2);

    assertThat(registry.get(PrintFormat.class)).containsKeys("alias1", "alias2");
  }

  private record TestProvider(String alias) implements Provider<PrintFormat<?>> {

    @Override
    public Class<PrintFormat<?>> type() {
      @SuppressWarnings("unchecked")
      Class<PrintFormat<?>> type = (Class<PrintFormat<?>>) (Class<?>) PrintFormat.class;
      return type;
    }

    @Override
    public PrintFormat<?> create() {
      return new SingleLineStringPrintFormat();
    }
  }
}
