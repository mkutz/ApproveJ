package org.approvej.scrub;

import java.util.function.Function;

/**
 * Scrubs a {@link String} by replacing all occurrences of a pattern by applying the given
 * replacement {@link Function} for each finding.
 */
public interface RegexScrubber extends Scrubber<String> {

  /**
   * Set the {@link Replacement} to be used.
   *
   * @param replacement a {@link Replacement} function
   * @return a copy of this using the given {@link #replacement}
   */
  RegexScrubber replacement(Replacement replacement);

  /**
   * Set the replacement {@link Function} always returning the given staticReplacement.
   *
   * @param staticReplacement the static replacement {@link String}
   * @return a copy of this using the given {@link #replacement}
   */
  RegexScrubber replacement(String staticReplacement);
}
