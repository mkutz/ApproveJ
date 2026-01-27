package org.approvej.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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
        .withMessageContaining("Failed to create print format");
  }

  @Test
  void resolve_invalidClassName_throwsError() {
    assertThatExceptionOfType(ConfigurationError.class)
        .isThrownBy(() -> Registry.resolve("org.nonexistent.Class", PrintFormat.class))
        .withMessageContaining("Failed to create print format");
  }
}
