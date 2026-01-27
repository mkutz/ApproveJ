package org.approvej.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Internal loader that chains multiple configuration sources. */
@NullMarked
final class ConfigurationLoader {

  private static final String ENV_PREFIX = "APPROVEJ_";
  private final List<ConfigurationSource> sources;

  private ConfigurationLoader(List<ConfigurationSource> sources) {
    this.sources = List.copyOf(sources);
  }

  @Nullable String get(String key) {
    for (ConfigurationSource source : sources) {
      String value = source.getValue(key);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  String get(String key, String defaultValue) {
    String value = get(key);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  static ConfigurationLoader createDefault() {
    return builder()
        .withEnvironmentVariables()
        .withClasspathProperties()
        .withUserHomeProperties()
        .build();
  }

  static Builder builder() {
    return new Builder();
  }

  static String toEnvironmentVariableName(String propertyName) {
    StringBuilder result = new StringBuilder(ENV_PREFIX);
    for (char c : propertyName.toCharArray()) {
      if (Character.isUpperCase(c)) {
        result.append('_');
      }
      result.append(Character.toUpperCase(c));
    }
    return result.toString();
  }

  static final class Builder {
    private final List<ConfigurationSource> sources = new ArrayList<>();

    Builder withEnvironmentVariables() {
      return withEnvironmentVariables(System::getenv);
    }

    Builder withEnvironmentVariables(Function<String, @Nullable String> envLookup) {
      sources.add(key -> envLookup.apply(toEnvironmentVariableName(key)));
      return this;
    }

    Builder withProperties(Properties properties) {
      sources.add(properties::getProperty);
      return this;
    }

    Builder withClasspathProperties() {
      URL url = Configuration.class.getClassLoader().getResource("approvej.properties");
      if (url != null) {
        try (InputStream is = url.openStream()) {
          Properties props = new Properties();
          props.load(is);
          sources.add(props::getProperty);
        } catch (IOException e) {
          throw new ConfigurationError(
              "Failed to load approvej.properties from classpath resources", e);
        }
      }
      return this;
    }

    Builder withUserHomeProperties() {
      Path configPath =
          Path.of(System.getProperty("user.home")).resolve(".config/approvej/approvej.properties");
      if (Files.exists(configPath)) {
        try (InputStream is = Files.newInputStream(configPath)) {
          Properties props = new Properties();
          props.load(is);
          sources.add(props::getProperty);
        } catch (IOException e) {
          throw new ConfigurationError("Failed to load %s".formatted(configPath), e);
        }
      }
      return this;
    }

    ConfigurationLoader build() {
      return new ConfigurationLoader(sources);
    }
  }
}
