package org.approvej.scrub;

import java.time.ZonedDateTime;

/** Scrubs a {@link String} by replacing all occurrences of a date time pattern. */
public class InstantScrubber {

  private static final ZonedDateTime EXAMPLE_INSTANT = ZonedDateTime.now();

  private InstantScrubber() {
    // Utility class
  }
}
