package org.approvej.scrub;

import java.util.function.Function;

/**
 * Scrubs a {@link String} by replacing all occurrences of a pattern by applying the given
 * replacement {@link Function} for each finding.
 */
public interface StringScrubber extends Scrubber<StringScrubber, String, String> {}
