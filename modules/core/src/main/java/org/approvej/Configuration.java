package org.approvej;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.approvej.print.PrintFormat;
import org.approvej.print.ToStringPrinter;
import org.approvej.review.FileReviewer;
import org.approvej.review.FileReviewerScript;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Central configuration class for ApproveJ.
 *
 * <p>All properties have a default value, which can be overwritten in your
 * src/test/resources/approvej.properties or ~/.config/approvej/approvej.properties.
 *
 * @param defaultPrintFormat the {@link PrintFormat} that will be used if none is specified
 *     otherwise
 * @param defaultFileReviewer the {@link FileReviewer} that will be used if none is specified
 */
@NullMarked
public record Configuration(
    PrintFormat<Object> defaultPrintFormat, @Nullable FileReviewer defaultFileReviewer) {

  private static final String DEFAULT_PRINTER_PROPERTY = "defaultPrinter";
  private static final String DEFAULT_PRINT_FORMAT_PROPERTY = "defaultPrintFormat";
  private static final String DEFAULT_FILE_REVIEWER_SCRIPT_PROPERTY = "defaultFileReviewerScript";

  private static final Properties DEFAULTS = new Properties();

  static {
    DEFAULTS.setProperty(DEFAULT_PRINTER_PROPERTY, ToStringPrinter.class.getName());
  }

  /** The loaded {@link Configuration} object. */
  public static final Configuration configuration = loadConfiguration();

  private static Configuration loadConfiguration() {
    Properties properties = loadProperties();

    String defaultPrintFormat =
        properties.getProperty(
            DEFAULT_PRINT_FORMAT_PROPERTY, properties.getProperty(DEFAULT_PRINTER_PROPERTY));
    PrintFormat<Object> printFormat;
    try {
      // noinspection unchecked
      printFormat =
          (PrintFormat<Object>)
              Class.forName(defaultPrintFormat).getDeclaredConstructor().newInstance();
    } catch (ReflectiveOperationException e) {
      throw new ConfigurationError(
          "Failed to create print format %s".formatted(defaultPrintFormat), e);
    }

    String defaultFileReviewerScript =
        properties.getProperty(DEFAULT_FILE_REVIEWER_SCRIPT_PROPERTY);
    FileReviewerScript defaultFileReviewer = null;
    if (defaultFileReviewerScript != null) {
      defaultFileReviewer = new FileReviewerScript(defaultFileReviewerScript);
    }

    return new Configuration(printFormat, defaultFileReviewer);
  }

  private static Properties loadProperties() {
    Properties properties = new Properties(Configuration.DEFAULTS);

    File userHomeProperties =
        new File(System.getProperty("user.home"), ".config/approvej/approvej.properties");
    if (userHomeProperties.exists()) {
      try (FileInputStream fis = new FileInputStream(userHomeProperties)) {
        properties.load(fis);
      } catch (IOException e) {
        throw new ConfigurationError("Error loading configuration from home directory", e);
      }
    }

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
