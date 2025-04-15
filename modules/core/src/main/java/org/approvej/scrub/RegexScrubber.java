package org.approvej.scrub;

import static org.approvej.scrub.Replacements.numbered;
import static org.approvej.scrub.Replacements.string;

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

  private final Pattern pattern;
  private final Function<Integer, Object> replacement;

  /**
   * Creates a {@link RegexScrubberBuilder} with the given pattern and replacement {@link Function}.
   *
   * @param pattern the pattern matching the string to be scrubbed as {@link String}
   * @param replacement a function that receives the finding index and returns the replacement
   *     string
   * @see Pattern#compile(String)
   */
  RegexScrubber(Pattern pattern, Function<Integer, Object> replacement) {
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
          return replacement.apply(findings.get(group)).toString();
        };
    return matcher.replaceAll(replacer);
  }

  /** Builder for creating a {@link RegexScrubber}. */
  public static class RegexScrubberBuilder implements ScrubberBuilder<String> {

    private final Pattern pattern;
    private Function<Integer, Object> replacement = Replacements.numbered();

    RegexScrubberBuilder(Pattern pattern) {
      this.pattern = pattern;
    }

    /**
     * Set the replacement {@link Function} to be used.
     *
     * @param replacement a {@link Function} that receives the finding index and returns the
     *     replacement string
     * @return this
     */
    public RegexScrubberBuilder replacement(Function<Integer, Object> replacement) {
      this.replacement = replacement;
      return this;
    }

    /**
     * Set the replacement {@link Function} always returning the given staticReplacement.
     *
     * @param staticReplacement the static replacement {@link String}
     * @return this
     */
    public RegexScrubber replacement(String staticReplacement) {
      return new RegexScrubber(pattern, string(staticReplacement));
    }

    /**
     * Creates a new {@link Scrubber} to replace strings matching the {@link #pattern} with a
     * numbered replacement.
     *
     * @return a new {@link RegexScrubber} using the {@link Replacements#numbered()}.
     */
    public RegexScrubber withNumberedReplacement() {
      return new RegexScrubber(pattern, numbered());
    }

    @Override
    public RegexScrubber build() {
      return new RegexScrubber(pattern, replacement);
    }
  }
}
