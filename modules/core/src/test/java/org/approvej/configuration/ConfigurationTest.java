package org.approvej.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Map;
import java.util.Properties;
import org.approvej.print.SingleLineStringPrintFormat;
import org.approvej.review.AutomaticReviewer;
import org.approvej.review.NoneReviewer;
import org.approvej.review.Reviewer;
import org.junit.jupiter.api.Test;

class ConfigurationTest {

  @Test
  void configuration_isLoaded() {
    assertThat(Configuration.configuration).isNotNull();
    assertThat(Configuration.configuration.defaultPrintFormat()).isNotNull();
  }

  @Test
  void loadConfiguration_fallback_to_defaults() {
    ConfigurationLoader loader = ConfigurationLoader.builder().build();

    Configuration config = Configuration.loadConfiguration(loader);

    assertThat(config.defaultPrintFormat()).isInstanceOf(SingleLineStringPrintFormat.class);
    assertThat(config.defaultFileReviewer()).isInstanceOf(Reviewer.class);
    assertThat(config.defaultInlineValueReviewer()).isInstanceOf(NoneReviewer.class);
  }

  @Test
  void loadConfiguration_defaultInlineValueReviewer() {
    Properties properties = new Properties();
    properties.setProperty("defaultInlineValueReviewer", "automatic");
    ConfigurationLoader loader = ConfigurationLoader.builder().withProperties(properties).build();

    Configuration config = Configuration.loadConfiguration(loader);

    assertThat(config.defaultInlineValueReviewer()).isInstanceOf(AutomaticReviewer.class);
  }

  @Test
  void loadConfiguration_customPrintFormat_from_properties() {
    Properties properties = new Properties();
    properties.setProperty("defaultPrintFormat", SingleLineStringPrintFormat.class.getName());
    ConfigurationLoader loader = ConfigurationLoader.builder().withProperties(properties).build();

    Configuration config = Configuration.loadConfiguration(loader);

    assertThat(config.defaultPrintFormat()).isInstanceOf(SingleLineStringPrintFormat.class);
    assertThat(config.defaultFileReviewer()).isInstanceOf(Reviewer.class);
  }

  @Test
  void loadConfiguration_environment_variable_overrides_properties() {
    Map<String, String> env =
        Map.of("APPROVEJ_DEFAULT_PRINT_FORMAT", SingleLineStringPrintFormat.class.getName());
    Properties properties = new Properties();
    properties.setProperty("defaultPrintFormat", "org.nonexistent.Format");
    ConfigurationLoader loader =
        ConfigurationLoader.builder()
            .withEnvironmentVariables(env::get)
            .withProperties(properties)
            .build();

    Configuration config = Configuration.loadConfiguration(loader);

    assertThat(config.defaultPrintFormat()).isInstanceOf(SingleLineStringPrintFormat.class);
  }

  @Test
  void loadConfiguration_reviewerScript() {
    Properties properties = new Properties();
    properties.setProperty("defaultFileReviewer", "script");
    properties.setProperty("reviewerScript", "diff {receivedFile} {approvedFile}");

    ConfigurationLoader loader = ConfigurationLoader.builder().withProperties(properties).build();

    Configuration config = Configuration.loadConfiguration(loader);

    assertThat(config.defaultFileReviewer()).isNotNull();
  }

  @Test
  void loadConfiguration_invalidPrintFormatThrowsError() {
    Properties properties = new Properties();
    properties.setProperty("defaultPrintFormat", "org.nonexistent.InvalidFormat");

    ConfigurationLoader loader = ConfigurationLoader.builder().withProperties(properties).build();

    assertThatExceptionOfType(ConfigurationError.class)
        .isThrownBy(() -> Configuration.loadConfiguration(loader))
        .withMessageContaining("Failed to create PrintFormat")
        .withMessageContaining("org.nonexistent.InvalidFormat");
  }

  @Test
  void loadConfiguration_inventoryEnabled_defaults_to_true() {
    ConfigurationLoader loader = ConfigurationLoader.builder().build();

    Configuration config = Configuration.loadConfiguration(loader);

    assertThat(config.inventoryEnabled()).isTrue();
  }

  @Test
  void loadConfiguration_inventoryEnabled_defaults_to_false_in_ci() {
    Map<String, String> env = Map.of("CI", "true");
    ConfigurationLoader loader =
        ConfigurationLoader.builder().withEnvironmentVariables(env::get).build();

    Configuration config = Configuration.loadConfiguration(loader);

    assertThat(config.inventoryEnabled()).isFalse();
  }

  @Test
  void loadConfiguration_inventoryEnabled_from_properties() {
    Properties properties = new Properties();
    properties.setProperty("inventoryEnabled", "false");
    ConfigurationLoader loader = ConfigurationLoader.builder().withProperties(properties).build();

    Configuration config = Configuration.loadConfiguration(loader);

    assertThat(config.inventoryEnabled()).isFalse();
  }

  @Test
  void loadConfiguration_inventoryEnabled_env_overrides_properties() {
    Map<String, String> env = Map.of("APPROVEJ_INVENTORY_ENABLED", "false");
    Properties properties = new Properties();
    properties.setProperty("inventoryEnabled", "true");
    ConfigurationLoader loader =
        ConfigurationLoader.builder()
            .withEnvironmentVariables(env::get)
            .withProperties(properties)
            .build();

    Configuration config = Configuration.loadConfiguration(loader);

    assertThat(config.inventoryEnabled()).isFalse();
  }

  @Test
  void configurationLoader_priorityChain() {
    // Simulate: env > project properties > user home properties
    Map<String, String> env = Map.of(); // Empty - no env variable set

    Properties projectProps = new Properties();
    projectProps.setProperty("defaultPrintFormat", SingleLineStringPrintFormat.class.getName());

    Properties userHomeProps = new Properties();
    userHomeProps.setProperty("defaultPrintFormat", "org.other.Format");

    ConfigurationLoader loader =
        ConfigurationLoader.builder()
            .withEnvironmentVariables(env::get)
            .withProperties(projectProps) // Project has priority
            .withProperties(userHomeProps)
            .build();

    Configuration config = Configuration.loadConfiguration(loader);

    // Project overrides user home
    assertThat(config.defaultPrintFormat()).isInstanceOf(SingleLineStringPrintFormat.class);
  }

  @Test
  void configurationLoader_envOverridesAll() {
    Map<String, String> env =
        Map.of("APPROVEJ_DEFAULT_PRINT_FORMAT", SingleLineStringPrintFormat.class.getName());

    Properties projectProps = new Properties();
    projectProps.setProperty("defaultPrintFormat", "org.nonexistent.ProjectFormat");

    Properties userHomeProps = new Properties();
    userHomeProps.setProperty("defaultPrintFormat", "org.nonexistent.UserFormat");

    ConfigurationLoader loader =
        ConfigurationLoader.builder()
            .withEnvironmentVariables(env::get)
            .withProperties(projectProps)
            .withProperties(userHomeProps)
            .build();

    Configuration config = Configuration.loadConfiguration(loader);

    // Environment variable has highest priority
    assertThat(config.defaultPrintFormat()).isInstanceOf(SingleLineStringPrintFormat.class);
  }

  @Test
  void configurationLoader_get_returnsDefaultWhenNoSourcesConfigured() {
    ConfigurationLoader loader = ConfigurationLoader.builder().build();

    assertThat(loader.get("missingKey", "defaultValue")).isEqualTo("defaultValue");
  }

  @Test
  void configurationLoader_get_returnsDefaultWhenKeyNotFound() {
    Properties properties = new Properties();
    properties.setProperty("otherKey", "otherValue");

    ConfigurationLoader loader = ConfigurationLoader.builder().withProperties(properties).build();

    assertThat(loader.get("missingKey", "defaultValue")).isEqualTo("defaultValue");
  }

  @Test
  void configurationLoader_get_returnsValueFromProperties() {
    Properties properties = new Properties();
    properties.setProperty("myKey", "myValue");

    ConfigurationLoader loader = ConfigurationLoader.builder().withProperties(properties).build();

    assertThat(loader.get("myKey", "default")).isEqualTo("myValue");
  }

  @Test
  void configurationLoader_get_returnsNullDefaultWhenNotConfigured() {
    ConfigurationLoader loader = ConfigurationLoader.builder().build();

    assertThat(loader.get("missingKey", null)).isNull();
  }

  @Test
  void loadConfiguration_reviewerAiCommand() {
    Properties properties = new Properties();
    properties.setProperty("defaultFileReviewer", "ai");
    properties.setProperty("reviewerAiCommand", "claude -p");
    ConfigurationLoader loader = ConfigurationLoader.builder().withProperties(properties).build();

    Configuration config = Configuration.loadConfiguration(loader);

    assertThat(config.defaultFileReviewer().getClass().getSimpleName()).isEqualTo("AiReviewer");
  }

  @Test
  void loadConfiguration_reviewerScript_without_property_throws() {
    Properties properties = new Properties();
    properties.setProperty("defaultFileReviewer", "script");
    ConfigurationLoader loader = ConfigurationLoader.builder().withProperties(properties).build();

    assertThatExceptionOfType(ConfigurationError.class)
        .isThrownBy(() -> Configuration.loadConfiguration(loader))
        .withMessageContaining("reviewerScript");
  }

  @Test
  void loadConfiguration_reviewerAiCommand_without_property_throws() {
    Properties properties = new Properties();
    properties.setProperty("defaultFileReviewer", "ai");
    ConfigurationLoader loader = ConfigurationLoader.builder().withProperties(properties).build();

    assertThatExceptionOfType(ConfigurationError.class)
        .isThrownBy(() -> Configuration.loadConfiguration(loader))
        .withMessageContaining("reviewerAiCommand");
  }

  @Test
  void configurationLoader_chainedSources_firstNonNullWins() {
    Properties properties1 = new Properties();
    properties1.setProperty("key1", "value1");
    properties1.setProperty("shared", "fromFirst");

    Properties properties2 = new Properties();
    properties2.setProperty("key2", "value2");
    properties2.setProperty("shared", "fromSecond");

    ConfigurationLoader loader =
        ConfigurationLoader.builder()
            .withProperties(properties1)
            .withProperties(properties2)
            .build();

    assertThat(loader.get("key1", null)).isEqualTo("value1");
    assertThat(loader.get("key2", null)).isEqualTo("value2");
    assertThat(loader.get("shared", null)).isEqualTo("fromFirst");
  }

  @Test
  void toEnvironmentVariableName() {
    assertThat(ConfigurationLoader.toEnvironmentVariableName("defaultPrintFormat"))
        .isEqualTo("APPROVEJ_DEFAULT_PRINT_FORMAT");
    assertThat(ConfigurationLoader.toEnvironmentVariableName("timeout"))
        .isEqualTo("APPROVEJ_TIMEOUT");
    assertThat(ConfigurationLoader.toEnvironmentVariableName("maxRetryCount"))
        .isEqualTo("APPROVEJ_MAX_RETRY_COUNT");
  }
}
