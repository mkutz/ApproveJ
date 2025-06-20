package org.approvej.scrub;

import static org.approvej.scrub.Scrubbers.stringsMatching;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RegexScrubberTest {

  @ParameterizedTest(name = "apply({0}) == {3}")
  @CsvSource({
    "'Hello World!', 'World', '%scrubbed%', 'Hello %scrubbed%!'",
    "'Hello World!', 'l', '_', 'He__o Wor_d!'",
    "'Hello World!', '[aeiou]', '', 'Hll Wrld!'"
  })
  void apply_static_replacement(
      String unscrubbedValue, Pattern regex, String replacement, String expected) {
    String scrubbedValue = stringsMatching(regex).replacement(replacement).apply(unscrubbedValue);

    assertThat(scrubbedValue).isEqualTo(expected);
  }

  @ParameterizedTest(name = "apply({0}) == {0}")
  @CsvSource({"'Hello World!', 'Foobar'", "'Hello World!', 'X'"})
  void apply_unmatched(String unscrubbedValue, Pattern notMatchingRegex) {
    String scrubbedValue = stringsMatching(notMatchingRegex).apply(unscrubbedValue);

    assertThat(scrubbedValue).isEqualTo(unscrubbedValue);
  }

  @Test
  void apply_custom_replacement() {
    RegexScrubber scrubber =
        stringsMatching("[aeiou]").replacement((match, count) -> "<vowel%d>".formatted(count));

    assertThat(scrubber.apply("Hello World!")).isEqualTo("H<vowel1>ll<vowel2> W<vowel2>rld!");
  }

  @Test
  void apply_default_replacement() {
    String scrubbedValue = stringsMatching("World").apply("Hello World!");

    assertThat(scrubbedValue).isEqualTo("Hello [scrubbed 1]!");
  }
}
