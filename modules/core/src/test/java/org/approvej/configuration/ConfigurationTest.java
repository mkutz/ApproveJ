package org.approvej.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Map;
import java.util.Properties;
import org.approvej.print.SingleLineStringPrintFormat;
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
  }

  @Test
  void loadConfiguration_customPrintFormat_from_properties() {
    Properties props = new Properties();
    props.setProperty("defaultPrintFormat", SingleLineStringPrintFormat.class.getName());
    ConfigurationLoader loader = ConfigurationLoader.builder().withProperties(props).build();

    Configuration config = Configuration.loadConfiguration(loader);

    assertThat(config.defaultPrintFormat()).isInstanceOf(SingleLineStringPrintFormat.class);
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
  void loadConfiguration_fileReviewerScript() {
    Properties props = new Properties();
    props.setProperty("defaultFileReviewerScript", "diff {receivedFile} {approvedFile}");

    ConfigurationLoader loader = ConfigurationLoader.builder().withProperties(props).build();

    Configuration config = Configuration.loadConfiguration(loader);

    assertThat(config.defaultFileReviewer()).isNotNull();
  }

  @Test
  void loadConfiguration_invalidPrintFormatThrowsError() {
    Properties props = new Properties();
    props.setProperty("defaultPrintFormat", "org.nonexistent.InvalidFormat");

    ConfigurationLoader loader = ConfigurationLoader.builder().withProperties(props).build();

    assertThatExceptionOfType(ConfigurationError.class)
        .isThrownBy(() -> Configuration.loadConfiguration(loader))
        .withMessageContaining("Failed to create print format")
        .withMessageContaining("org.nonexistent.InvalidFormat");
  }

  @Test
  void configurationLoader_priorityChain() {
    // Simulate: env > project props > user home props
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
  void toEnvironmentVariableName() {
    assertThat(ConfigurationLoader.toEnvironmentVariableName("defaultPrintFormat"))
        .isEqualTo("APPROVEJ_DEFAULT_PRINT_FORMAT");
    assertThat(ConfigurationLoader.toEnvironmentVariableName("timeout"))
        .isEqualTo("APPROVEJ_TIMEOUT");
    assertThat(ConfigurationLoader.toEnvironmentVariableName("maxRetryCount"))
        .isEqualTo("APPROVEJ_MAX_RETRY_COUNT");
  }

  @Test
  void configurationLoader_get_returnsDefaultWhenNoSourcesConfigured() {
    ConfigurationLoader loader = ConfigurationLoader.builder().build();

    assertThat(loader.get("missingKey", "defaultValue")).isEqualTo("defaultValue");
  }

  @Test
  void configurationLoader_get_returnsDefaultWhenKeyNotFound() {
    Properties props = new Properties();
    props.setProperty("otherKey", "otherValue");

    ConfigurationLoader loader = ConfigurationLoader.builder().withProperties(props).build();

    assertThat(loader.get("missingKey", "defaultValue")).isEqualTo("defaultValue");
  }

  @Test
  void configurationLoader_get_returnsValueFromProperties() {
    Properties props = new Properties();
    props.setProperty("myKey", "myValue");

    ConfigurationLoader loader = ConfigurationLoader.builder().withProperties(props).build();

    assertThat(loader.get("myKey", "default")).isEqualTo("myValue");
  }

  @Test
  void configurationLoader_get_returnsNullDefaultWhenNotConfigured() {
    ConfigurationLoader loader = ConfigurationLoader.builder().build();

    assertThat(loader.get("missingKey", null)).isNull();
  }

  @Test
  void configurationLoader_chainedSources_firstNonNullWins() {
    Properties props1 = new Properties();
    props1.setProperty("key1", "value1");
    props1.setProperty("shared", "fromFirst");

    Properties props2 = new Properties();
    props2.setProperty("key2", "value2");
    props2.setProperty("shared", "fromSecond");

    ConfigurationLoader loader =
        ConfigurationLoader.builder().withProperties(props1).withProperties(props2).build();

    assertThat(loader.get("key1", null)).isEqualTo("value1");
    assertThat(loader.get("key2", null)).isEqualTo("value2");
    assertThat(loader.get("shared", null)).isEqualTo("fromFirst");
  }

  @Test
  void configurationLoader_emptyBuilder_returnsLoaderWithNoSources() {
    ConfigurationLoader loader = ConfigurationLoader.builder().build();

    assertThat(loader.get("anyKey", "default")).isEqualTo("default");
  }
}
