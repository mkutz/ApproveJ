package org.approvej.scrub;

import static org.approvej.scrub.Replacements.string;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.NullMarked;

/**
 * Scrubs a {@link String} by replacing all occurrences of a pattern by applying the given
 * replacement {@link Function} for each finding.
 *
 * @param pattern     the pattern matching the string to be scrubbed as {@link String}
 * @param replacement a {@link Replacement} function
 * @see Pattern#compile(String)
 */
@NullMarked
public record RegexScrubber(Pattern pattern, Replacement replacement) implements Scrubber<String> {

  @Override
  public String apply(String unscrubbedValue) {
    Matcher matcher = pattern.matcher(unscrubbedValue);
    Map<String, Integer> findings = new HashMap<>();
    Function<MatchResult, String> replacer =
        result -> {
          String group = result.group();
          findings.putIfAbsent(group, findings.size() + 1);
          return replacement.apply(group, findings.get(group));
        };
    return matcher.replaceAll(replacer);
  }

  /**
   * Set the {@link Replacement} to be used.
   *
   * @param replacement a {@link Replacement} function
   * @return this
   */
  public RegexScrubber replacement(Replacement replacement) {
    return new RegexScrubber(pattern, replacement);
  }

  /**
   * Set the replacement {@link Function} always returning the given staticReplacement.
   *
   * @param staticReplacement the static replacement {@link String}
   * @return this
   */
  public RegexScrubber replacement(String staticReplacement) {
    return replacement(string(staticReplacement));
  }
}
