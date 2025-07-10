package org.approvej.scrub;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import org.jspecify.annotations.NullMarked;

/**
 * Scrubs a {@link String} by replacing all occurrences of a date pattern with relative
 * descriptions, like {@code [today]}, {@code [yesterday]}, {@code [2 days from now]} , {@code [21
 * days ago]}.
 */
@NullMarked
public class RelativeDateScrubber implements Scrubber<String> {

  private static final LocalDate EXAMPLE_DATE = LocalDate.of(4567, 12, 30);

  private final Pattern pattern;
  private final UnaryOperator<String> replacement;

  RelativeDateScrubber(DateTimeFormatter dateFormatPattern) {
    pattern =
        Pattern.compile(
            dateFormatPattern
                .format(EXAMPLE_DATE)
                .replaceAll("\\p{L}+", "\\\\p{L}+")
                .replaceAll("\\d", "\\\\d"));
    replacement =
        (String finding) -> {
          LocalDate parsed = dateFormatPattern.parse(finding, LocalDate::from);
          long days =
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
