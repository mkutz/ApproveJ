package org.approvej.scrub;

import java.util.function.Function;
import org.jspecify.annotations.NullMarked;

/**
 * Scrubs a {@link String} by replacing all occurrences of a pattern by applying the given
 * replacement {@link Function} for each finding.
 */
@NullMarked
public interface StringScrubber extends Scrubber<StringScrubber, String, String> {}
