package org.approvej;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.approvej.print.Printer;
import org.approvej.print.ToStringPrinter;
import org.jspecify.annotations.NullMarked;

/**
 * Central configuration class for ApproveJ.
 *
 * <p>All properties have a default value, which can be overwritten in your
 * src/test/resources/approvej.properties
 *
 * @param defaultPrinter the {@link Printer} that will be used if none is specified otherwise
 */
@NullMarked
public record Configuration(Printer<Object> defaultPrinter) {

  /** The loaded {@link Configuration} object. */
  public static final Configuration configuration = loadConfiguration();

  private static final Properties DEFAULTS = new Properties();

  static {
    DEFAULTS.setProperty("defaultPrinter", ToStringPrinter.class.getName());
  }

  private static Configuration loadConfiguration() {
    Properties properties = loadProperties();

    String defaultPrinter = properties.getProperty("defaultPrinter");
    Printer<Object> printer;
    try {
      // noinspection unchecked
      printer =
          (Printer<Object>) Class.forName(defaultPrinter).getDeclaredConstructor().newInstance();
    } catch (ReflectiveOperationException e) {
      throw new ConfigurationError("Failed to create printer %s".formatted(defaultPrinter), e);
    }

    return new Configuration(printer);
  }

  private static Properties loadProperties() {
    Properties properties = new Properties(Configuration.DEFAULTS);
    try (InputStream inputStream =
        Configuration.class.getClassLoader().getResourceAsStream("approvej.properties")) {
      if (inputStream != null) {
        properties.load(inputStream);
      }
    } catch (IOException e) {
      throw new ConfigurationError("Failed to load configuration", e);
    }
    return properties;
  }
}
