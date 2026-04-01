package org.approvej.configuration;

import org.approvej.print.PrintFormat;
import org.approvej.print.SingleLineStringPrintFormat;
import org.approvej.review.Reviewer;
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
 *       "none", "automatic", "script", "ai" for reviewers)
 *   <li>Fully-qualified class names (for backward compatibility and custom implementations)
 * </ul>
 *
 * <p>The "script" reviewer requires the {@code reviewerScript} property to be set. The "ai"
 * reviewer requires the {@code reviewerAiCommand} property to be set.
 *
 * @param defaultPrintFormat the {@link PrintFormat} that will be used if none is specified
 *     otherwise
 * @param defaultFileReviewer the {@link Reviewer} that will be used for file-based approvals if
 *     none is specified
 * @param inventoryEnabled whether the approved file inventory is enabled
 * @param defaultInlineValueReviewer the {@link Reviewer} that will be used for inline value
 *     approvals if none is specified
 */
@NullMarked
public record Configuration(
    PrintFormat<Object> defaultPrintFormat,
    Reviewer defaultFileReviewer,
    boolean inventoryEnabled,
    Reviewer defaultInlineValueReviewer) {

  private static final String DEFAULT_PRINT_FORMAT_PROPERTY = "defaultPrintFormat";
  private static final String DEFAULT_FILE_REVIEWER_PROPERTY = "defaultFileReviewer";
  private static final String REVIEWER_SCRIPT_PROPERTY = "reviewerScript";
  private static final String REVIEWER_AI_COMMAND_PROPERTY = "reviewerAiCommand";
  private static final String INVENTORY_ENABLED_PROPERTY = "inventoryEnabled";
  private static final String DEFAULT_INLINE_VALUE_REVIEWER_PROPERTY = "defaultInlineValueReviewer";

  /** The loaded {@link Configuration} object. */
  public static final Configuration configuration =
      loadConfiguration(ConfigurationLoader.createDefault());

  static Configuration loadConfiguration(ConfigurationLoader loader) {
    String printFormatConfig = loader.get(DEFAULT_PRINT_FORMAT_PROPERTY, "singleLineString");
    PrintFormat<Object> printFormat = resolvePrintFormat(printFormatConfig);

    Reviewer fileReviewer =
        resolveReviewer(loader, loader.get(DEFAULT_FILE_REVIEWER_PROPERTY, "none"));

    boolean inventoryEnabled = resolveInventoryEnabled(loader);

    Reviewer inlineValueReviewer =
        resolveReviewer(loader, loader.get(DEFAULT_INLINE_VALUE_REVIEWER_PROPERTY, "none"));

    return new Configuration(printFormat, fileReviewer, inventoryEnabled, inlineValueReviewer);
  }

  @SuppressWarnings("unchecked")
  private static PrintFormat<Object> resolvePrintFormat(@Nullable String aliasOrClassName) {
    if (aliasOrClassName == null) {
      return new SingleLineStringPrintFormat();
    }
    return Registry.resolve(aliasOrClassName, PrintFormat.class);
  }

  private static Reviewer resolveReviewer(ConfigurationLoader loader, String aliasOrClassName) {
    if ("script".equals(aliasOrClassName)) {
      String script = loader.get(REVIEWER_SCRIPT_PROPERTY);
      if (script == null) {
        throw new ConfigurationError(
            "Reviewer 'script' requires the '%s' property to be set"
                .formatted(REVIEWER_SCRIPT_PROPERTY),
            null);
      }
      return Reviewers.script(script);
    }
    if ("ai".equals(aliasOrClassName)) {
      String aiCommand = loader.get(REVIEWER_AI_COMMAND_PROPERTY);
      if (aiCommand == null) {
        throw new ConfigurationError(
            "Reviewer 'ai' requires the '%s' property to be set"
                .formatted(REVIEWER_AI_COMMAND_PROPERTY),
            null);
      }
      return Reviewers.ai(aiCommand);
    }
    return Registry.resolve(aliasOrClassName, Reviewer.class);
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
