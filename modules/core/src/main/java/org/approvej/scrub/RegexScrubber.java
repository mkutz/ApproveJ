package org.approvej.scrub;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.jspecify.annotations.NullMarked;

/**
 * Scrubs a {@link String} by replacing all occurrences of a pattern by applying the given
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
   * Creates a {@link RegexScrubberBuilder} with the given pattern.
   *
   * @param pattern the {@link Pattern} matching the strings to be scrubbed
   * @return a {@link RegexScrubberBuilder} with the given pattern
   */
  public static RegexScrubberBuilder stringsMatching(Pattern pattern) {
    return new RegexScrubberBuilder(pattern);
  }

  /**
   * Creates a {@link RegexScrubberBuilder} with the given pattern.
   *
   * @param pattern the pattern matching the string to be scrubbed as {@link String}
   * @return a {@link RegexScrubberBuilder} with the given pattern
   * @see Pattern#compile(String)
   */
  public static RegexScrubberBuilder stringsMatching(String pattern) {
    return new RegexScrubberBuilder(Pattern.compile(pattern));
  }

  protected RegexScrubber(Pattern pattern, Function<Integer, String> replacement) {
    this.pattern = pattern;
    this.replacement = replacement;
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

  /** Builder for creating a {@link RegexScrubber}. */
  public static class RegexScrubberBuilder {
    private final Pattern pattern;

    private RegexScrubberBuilder(Pattern pattern) {
      this.pattern = pattern;
    }

    /**
     * Create a {@link Scrubber} to replace any match of the {@link #pattern} with the result of the
     * given replacement {@link Function}.
     *
     * @param replacement a function that receives the finding index and returns the replacement
     *     string
     * @return a {@link RegexScrubber} to replace any match of the {@link #pattern} with the result
     *     of the given replacement {@link Function}.
     */
    public RegexScrubber with(Function<Integer, String> replacement) {
      return new RegexScrubber(pattern, replacement);
    }

    /**
     * Creates a new {@link Scrubber} to replace any match of the {@link #pattern} with the given
     * static replacement.
     *
     * @param staticReplacement the static replacement {@link String}
     * @return a new {@link RegexScrubber} to replace any match of the {@link #pattern} with the
     *     given staticReplacement.
     */
    public RegexScrubber with(String staticReplacement) {
      return new RegexScrubber(pattern, number -> staticReplacement);
    }

    /**
     * Creates a new {@link Scrubber} to replace strings matching the {@link #pattern} with a
     * numbered replacement.
     *
     * @return a new {@link RegexScrubber} using the {@link #NUMBERED_REPLACEMENT}.
     */
    public RegexScrubber withNumberedReplacement() {
      return new RegexScrubber(pattern, NUMBERED_REPLACEMENT);
    }
  }
}
