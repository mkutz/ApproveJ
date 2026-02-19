package org.approvej.configuration;

import org.approvej.print.PrintFormat;
import org.approvej.print.SingleLineStringPrintFormat;
import org.approvej.review.FileReviewer;
import org.approvej.review.Reviewers;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Central configuration class for ApproveJ.
 *
 * <p>Configuration values can be set through multiple mechanisms, with the following priority
 * (highest to lowest):
 *
 * <ol>
 *   <li>Environment variables (e.g., {@code APPROVEJ_DEFAULT_PRINT_FORMAT})
 *   <li>Project properties ({@code src/test/resources/approvej.properties})
 *   <li>User home properties ({@code ~/.config/approvej/approvej.properties})
 *   <li>Default values
 * </ol>
 *
 * <p>Print formats and reviewers can be configured using either:
 *
 * <ul>
 *   <li>Aliases (e.g., "json", "yaml", "singleLineString", "multiLineString" for print formats;
 *       "none", "automatic" for reviewers)
 *   <li>Fully-qualified class names (for backward compatibility and custom implementations)
 * </ul>
 *
 * @param defaultPrintFormat the {@link PrintFormat} that will be used if none is specified
 *     otherwise
 * @param defaultFileReviewer the {@link FileReviewer} that will be used if none is specified
 * @param inventoryEnabled whether the approved file inventory is enabled
 */
@NullMarked
public record Configuration(
    PrintFormat<Object> defaultPrintFormat,
    FileReviewer defaultFileReviewer,
    boolean inventoryEnabled) {

  private static final String DEFAULT_PRINT_FORMAT_PROPERTY = "defaultPrintFormat";
  private static final String DEFAULT_FILE_REVIEWER_PROPERTY = "defaultFileReviewer";
  private static final String DEFAULT_FILE_REVIEWER_SCRIPT_PROPERTY = "defaultFileReviewerScript";
  private static final String INVENTORY_ENABLED_PROPERTY = "inventoryEnabled";

  /** The loaded {@link Configuration} object. */
  public static final Configuration configuration =
      loadConfiguration(ConfigurationLoader.createDefault());

  static Configuration loadConfiguration(ConfigurationLoader loader) {
    String printFormatConfig = loader.get(DEFAULT_PRINT_FORMAT_PROPERTY, "singleLineString");
    PrintFormat<Object> printFormat = resolvePrintFormat(printFormatConfig);

    FileReviewer fileReviewer = resolveFileReviewer(loader);

    boolean inventoryEnabled = resolveInventoryEnabled(loader);

    return new Configuration(printFormat, fileReviewer, inventoryEnabled);
  }

  @SuppressWarnings("unchecked")
  private static PrintFormat<Object> resolvePrintFormat(@Nullable String aliasOrClassName) {
    if (aliasOrClassName == null) {
      return new SingleLineStringPrintFormat();
    }
    return Registry.resolve(aliasOrClassName, PrintFormat.class);
  }

  private static FileReviewer resolveFileReviewer(ConfigurationLoader loader) {
    String fileReviewerScript = loader.get(DEFAULT_FILE_REVIEWER_SCRIPT_PROPERTY);
    if (fileReviewerScript != null) {
      return Reviewers.script(fileReviewerScript);
    }

    return Registry.resolve(loader.get(DEFAULT_FILE_REVIEWER_PROPERTY, "none"), FileReviewer.class);
  }

  private static boolean resolveInventoryEnabled(ConfigurationLoader loader) {
    String configured = loader.get(INVENTORY_ENABLED_PROPERTY);
    if (configured != null) {
      return Boolean.parseBoolean(configured);
    }
    String ci = loader.getenv("CI");
    return ci == null || ci.isBlank();
  }
}
