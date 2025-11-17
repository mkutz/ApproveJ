package org.approvej.scrub;

import static org.approvej.scrub.Replacements.string;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.NullMarked;

@NullMarked
record RegexScrubberRecord(Pattern pattern, Replacement replacement) implements RegexScrubber {

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

  @Override
  public RegexScrubber replacement(Replacement replacement) {
    return new org.approvej.scrub.RegexScrubberRecord(pattern, replacement);
  }

  @Override
  public RegexScrubber replacement(String staticReplacement) {
    return replacement(string(staticReplacement));
  }
}
