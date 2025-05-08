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

  private static Configuration loadConfiguration() {
    Properties properties = new Properties();
    try (InputStream is =
        Configuration.class.getClassLoader().getResourceAsStream("approvej.properties")) {
      if (is != null) {
        properties.load(is);

        Printer<Object> printer = new ToStringPrinter();
        try {
          Class<?> defaultPrinterClass = Class.forName(properties.getProperty("defaultPrinter"));
          if (Printer.class.isAssignableFrom(defaultPrinterClass)) {
            // noinspection unchecked
            printer = (Printer<Object>) defaultPrinterClass.getDeclaredConstructor().newInstance();
          } else {
            throw new ConfigurationError("Configured printer does not implement Printer<Object>");
          }
        } catch (ReflectiveOperationException e) {
          throw new ConfigurationError("Failed to create printer", e);
        }

        return new Configuration(printer);
      }
    } catch (IOException e) {
      throw new ConfigurationError("Failed to load configuration", e);
    }

    return new Configuration(new ToStringPrinter());
  }
}
