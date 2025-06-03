package org.approvej;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.approvej.print.Printer;
import org.approvej.print.ToStringPrinter;
import org.approvej.review.FileReviewer;
import org.approvej.review.FileReviewerScript;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Central configuration class for ApproveJ.
 *
 * <p>All properties have a default value, which can be overwritten in your
 * src/test/resources/approvej.properties
 *
 * @param defaultPrinter the {@link Printer} that will be used if none is specified otherwise
 * @param defaultFileReviewer the {@link FileReviewer} that will be used if none is specified
 */
@NullMarked
public record Configuration(
    Printer<Object> defaultPrinter, @Nullable FileReviewer defaultFileReviewer) {

  private static final Properties DEFAULTS = new Properties();

  static {
    DEFAULTS.setProperty("defaultPrinter", ToStringPrinter.class.getName());
  }

  /** The loaded {@link Configuration} object. */
  public static final Configuration configuration = loadConfiguration();

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

    String defaultFileReviewerScript = properties.getProperty("defaultFileReviewerScript");
    FileReviewerScript defaultFileReviewer = null;
    if (defaultFileReviewerScript != null) {
      defaultFileReviewer = new FileReviewerScript(defaultFileReviewerScript);
    }

    return new Configuration(printer, defaultFileReviewer);
  }

  private static Properties loadProperties() {
    Properties properties = new Properties(Configuration.DEFAULTS);
    URL configurationFileUrl =
        Configuration.class.getClassLoader().getResource("approvej.properties");
    if (configurationFileUrl == null) {
      return properties;
    }
    try (InputStream inputStream = configurationFileUrl.openStream()) {
      if (inputStream != null) {
        properties.load(inputStream);
      }
    } catch (IOException e) {
      throw new ConfigurationError("Failed to load configuration", e);
    }
    return properties;
  }
}
