package org.approvej.configuration;

import org.approvej.print.PrintFormat;
import org.approvej.print.SingleLineStringPrintFormat;
import org.approvej.review.FileReviewer;
import org.approvej.review.FileReviewerScript;
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
 * @param defaultPrintFormat the {@link PrintFormat} that will be used if none is specified
 *     otherwise
 * @param defaultFileReviewer the {@link FileReviewer} that will be used if none is specified
 */
@NullMarked
public record Configuration(
    PrintFormat<Object> defaultPrintFormat, @Nullable FileReviewer defaultFileReviewer) {

  private static final String DEFAULT_PRINT_FORMAT_PROPERTY = "defaultPrintFormat";
  private static final String DEFAULT_FILE_REVIEWER_SCRIPT_PROPERTY = "defaultFileReviewerScript";

  /** The loaded {@link Configuration} object. */
  public static final Configuration configuration =
      loadConfiguration(ConfigurationLoader.createDefault());

  static Configuration loadConfiguration(ConfigurationLoader loader) {
    String defaultPrintFormatClass =
        loader.get(DEFAULT_PRINT_FORMAT_PROPERTY, SingleLineStringPrintFormat.class.getName());
    PrintFormat<Object> printFormat = createPrintFormat(defaultPrintFormatClass);

    String defaultFileReviewerScript = loader.get(DEFAULT_FILE_REVIEWER_SCRIPT_PROPERTY, null);
    FileReviewerScript defaultFileReviewer = null;
    if (defaultFileReviewerScript != null) {
      defaultFileReviewer = new FileReviewerScript(defaultFileReviewerScript);
    }

    return new Configuration(printFormat, defaultFileReviewer);
  }

  @SuppressWarnings("unchecked")
  private static PrintFormat<Object> createPrintFormat(@Nullable String defaultPrintFormat) {
    if (defaultPrintFormat == null) {
      return new SingleLineStringPrintFormat();
    }
    PrintFormat<Object> printFormat;
    try {
      printFormat =
          (PrintFormat<Object>)
              Class.forName(defaultPrintFormat).getDeclaredConstructor().newInstance();
    } catch (ReflectiveOperationException e) {
      throw new ConfigurationError(
          "Failed to create print format %s".formatted(defaultPrintFormat), e);
    }
    return printFormat;
  }
}
