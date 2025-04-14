package org.approvej.scrub;

import static org.approvej.scrub.RegexScrubber.stringsMatching;
import static org.approvej.scrub.Replacements.numbered;

import java.util.function.Function;
import java.util.regex.Pattern;
import org.jspecify.annotations.NullMarked;

/**
 * Scrubs a {@link String} by replacing all occurrences UUIDs.
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
 * <p>Will be scrubbed to
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
public class UuidScrubber {

  private static final Pattern UUID_PATTERN =
      Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

  /**
   * Creates a new {@link RegexScrubber} using the given {@code replacement} function.
   *
   * @param replacement a custom replacement function that takes the number of the match as an
   *     argument
   * @return a RegexScrubber that replaces all UUIDs with the given replacement
   */
  public static RegexScrubber uuids(Function<Integer, Object> replacement) {
    return stringsMatching(UUID_PATTERN).with(replacement);
  }

  /**
   * Creates a new {@link RegexScrubber} using the {@link Replacements#numbered()}.
   *
   * @return a RegexScrubber that replaces all UUIDs with a numbered placeholder
   */
  public static RegexScrubber uuids() {
    return stringsMatching(UUID_PATTERN).with(numbered("uuid"));
  }

  private UuidScrubber() {
    // Utility class
  }
}
