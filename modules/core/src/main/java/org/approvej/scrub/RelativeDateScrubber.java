package org.approvej.scrub;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Scrubs a {@link CharSequence} by replacing all occurrences of a date pattern with relative
 * descriptions, like <code>[today]</code>, <code>[yesterday]</code>, <code>[2 days from now]</code>
 * .
 */
public class RelativeDateScrubber implements Scrubber<String> {

  private static final LocalDate EXAMPLE_DATE = LocalDate.of(4567, 12, 30);

  private final Pattern pattern;
  private final UnaryOperator<String> replacement;

  /**
   * Creates {@link Scrubber} to replace date strings of the given pattern.
   *
   * @param dateFormatPattern a {@link DateTimeFormatter} to parse the dates
   * @return a new {@link RelativeDateScrubber} with the given {@link DateTimeFormatter}.
   */
  public static RelativeDateScrubber relativeDates(DateTimeFormatter dateFormatPattern) {
    return new RelativeDateScrubber(dateFormatPattern);
  }

  private RelativeDateScrubber(DateTimeFormatter dateFormatPattern) {
    pattern =
        Pattern.compile(
            dateFormatPattern
                .format(EXAMPLE_DATE)
                .replaceAll("\\p{L}+", "\\\\p{L}+")
                .replaceAll("\\d", "\\\\d"));
    replacement =
        (String finding) -> {
          var parsed = dateFormatPattern.parse(finding, LocalDate::from);
          var days =
              Duration.between(LocalDate.now().atStartOfDay(), parsed.atStartOfDay()).toDays();
          if (days == 0) return "[today]";
          else if (days == 1) return "[tomorrow]";
          else if (days == -1) return "[yesterday]";
          else if (days > 1) return "[%d days from now]".formatted(days);
          else return "[%d days ago]".formatted(-days);
        };
  }

  @Override
  public String apply(String unscrubbedValue) {
    return pattern.matcher(unscrubbedValue).replaceAll(result -> replacement.apply(result.group()));
  }
}
