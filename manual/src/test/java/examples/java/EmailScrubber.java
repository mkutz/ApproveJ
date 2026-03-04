package examples.java;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.approvej.scrub.Replacement;
import org.approvej.scrub.Replacements;
import org.approvej.scrub.StringScrubber;

public class EmailScrubber implements StringScrubber { // <1>

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

  private final Replacement<String> replacement;

  public EmailScrubber() {
    this(Replacements.numbered("email")); // <2>
  }

  private EmailScrubber(Replacement<String> replacement) {
    this.replacement = replacement;
  }

  @Override
  public String apply(String value) { // <3>
    Map<String, Integer> findings = new HashMap<>();
    Function<MatchResult, String> replacer =
        result -> {
          String group = result.group();
          findings.putIfAbsent(group, findings.size() + 1);
          return String.valueOf(replacement.apply(group, findings.get(group)));
        };
    return EMAIL_PATTERN.matcher(value).replaceAll(replacer);
  }

  @Override
  public StringScrubber replacement(Replacement<String> replacement) { // <4>
    return new EmailScrubber(replacement);
  }
}
