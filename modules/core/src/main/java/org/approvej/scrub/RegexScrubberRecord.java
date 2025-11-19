package org.approvej.scrub;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.NullMarked;

@NullMarked
record RegexScrubberRecord(Pattern pattern, Replacement<String> replacement)
    implements StringScrubber {

  @Override
  public String apply(String unscrubbedValue) {
    Matcher matcher = pattern.matcher(unscrubbedValue);
    Map<String, Integer> findings = new HashMap<>();
    Function<MatchResult, String> replacer =
        result -> {
          String group = result.group();
          findings.putIfAbsent(group, findings.size() + 1);
          return String.valueOf(replacement.apply(group, findings.get(group)));
        };
    return matcher.replaceAll(replacer);
  }

  @Override
  public StringScrubber replacement(Replacement<String> replacement) {
    return new RegexScrubberRecord(pattern, replacement);
  }
}
