package org.approvej;

import java.util.function.Function;
import java.util.regex.Pattern;
import org.jspecify.annotations.NullMarked;

/**
 * Scrubs a {@link CharSequence} by replacing all occurrences UUIDs with a numbered placeholder.
 *
 * <p>E.g.
 *
 * <pre>
 *   {
 *     "id": "123e4567-e89b-12d3-a456-426614174000",
 *     "other-id": "123e4567-e89b-12d3-a456-426614174001",
 *     "same-id": "123e4567-e89b-12d3-a456-426614174000"
 *   }
 * </pre>
 *
 * <p>Will be scrubbed to</code>
 *
 * <pre>
 *   {
 *     "id": "[uuid 1]",
 *     "other-id": "[uuid 2]",
 *     "same-id": "[uuid 1]"
 *   }
 * </pre>
 */
@NullMarked
public class UuidScrubber extends RegexScrubber {

  /** Replaces each match with "[uuid #]" where '#' is the number of the distinct found string. */
  public static final Function<Integer, String> NUMBERED_REPLACEMENT = "[uuid %d]"::formatted;

  private static final Pattern UUID_PATTERN =
      Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

  public UuidScrubber(Function<Integer, String> replacement) {
    super(UUID_PATTERN, replacement);
  }

  /** Creates a new {@link UuidScrubber} using the {@link #NUMBERED_REPLACEMENT}. */
  public UuidScrubber() {
    this(NUMBERED_REPLACEMENT);
  }
}
