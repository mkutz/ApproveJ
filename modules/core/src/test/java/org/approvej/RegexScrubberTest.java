package org.approvej;

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
    var scrubbedValue = new RegexScrubber(regex, replacement).apply(unscrubbedValue);

    assertThat(scrubbedValue).isEqualTo(expected);
  }

  @ParameterizedTest(name = "apply({0}) == {0}")
  @CsvSource({"'Hello World!', 'Foobar'", "'Hello World!', 'X'"})
  void apply_unmatched(String unscrubbedValue, Pattern notMatchingRegex) {
    var scrubbedValue = new RegexScrubber(notMatchingRegex).apply(unscrubbedValue);

    assertThat(scrubbedValue).isEqualTo(unscrubbedValue);
  }

  @Test
  void apply_custom_replacement() {
    var scrubber = new RegexScrubber(Pattern.compile("[aeiou]"), "<vowel%d>"::formatted);

    assertThat(scrubber.apply("Hello World!")).isEqualTo("H<vowel1>ll<vowel2> W<vowel2>rld!");
  }

  @Test
  void apply_default_replacement() {
    var scrubbedValue = new RegexScrubber(Pattern.compile("World")).apply("Hello World!");

    assertThat(scrubbedValue).isEqualTo("Hello [scrubbed 1]!");
  }
}
