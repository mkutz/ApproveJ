package org.approvej;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.jspecify.annotations.NullMarked;

/**
 * Scrubs a {@link CharSequence} by replacing all occurrences of a pattern by applying the given
 * replacement {@link Function} for each finding.
 */
@NullMarked
public class RegexScrubber implements Scrubber<String> {

  /**
   * Replaces each match with "[scrubbed #]" where '#' is the number of the distinct found string.
   *
   * <p>E.g. <code>Hello World!</code> with a pattern of <code>[aeiou]</code> will result in <code>
   * "H[scrubbed 1]ll[scrubbed 2] W[scrubbed 2]rld!"</code>
   */
  public static final Function<Integer, String> NUMBERED_REPLACEMENT = "[scrubbed %d]"::formatted;

  private final Pattern pattern;
  private final Function<Integer, String> replacement;

  /**
   * @param pattern the {@link Pattern} to be scrubbed
   * @param replacement a function that receives the finding index and returns the replacement
   *     string
   */
  public RegexScrubber(Pattern pattern, Function<Integer, String> replacement) {
    this.pattern = pattern;
    this.replacement = replacement;
  }

  /**
   * Creates a new {@link RegexScrubber} which will replace any match of the given pattern with the
   * given staticReplacement.
   *
   * @param pattern the {@link Pattern} to be scrubbed
   * @param staticReplacement the static replacement {@link String}
   */
  public RegexScrubber(Pattern pattern, String staticReplacement) {
    this(pattern, number -> staticReplacement);
  }

  /**
   * Creates a new {@link RegexScrubber} using the {@link #NUMBERED_REPLACEMENT} .
   *
   * @param pattern the {@link Pattern} to be scrubbed
   */
  public RegexScrubber(Pattern pattern) {
    this(pattern, NUMBERED_REPLACEMENT);
  }

  @Override
  public String apply(String unscrubbedValue) {
    var matcher = pattern.matcher(unscrubbedValue);
    Map<String, Integer> findings = new HashMap<>();
    Function<MatchResult, String> replacer =
        result -> {
          String group = result.group();
          findings.putIfAbsent(group, findings.size() + 1);
          return replacement.apply(findings.get(group));
        };
    return matcher.replaceAll(replacer);
  }
}
