package org.approvej.json.jackson3;

import static org.approvej.scrub.Replacements.string;

import org.approvej.scrub.Scrubber;
import org.jspecify.annotations.NullMarked;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;

/**
 * A {@link Scrubber} that scrubs a JSON node at a specific {@link JsonPointer} by replacing its
 * target with a replacement.
 */
@NullMarked
public interface JsonPointerScrubber extends Scrubber<JsonPointerScrubber, JsonNode, String> {

  /**
   * Create a {@link JsonPointerScrubber} to replace the JSON node at the given {@link JsonPointer}
   * {@link String}.
   *
   * @param jsonPointerString a {@link String} compilable to a {@link JsonPointer} to be replaced
   * @return a new {@link JsonPointerScrubber}
   * @see JsonPointer#compile(String)
   */
  static JsonPointerScrubber jsonPointer(String jsonPointerString) {
    return new JsonPointerScrubberRecord(
        JsonPointer.compile(jsonPointerString), string("[scrubbed]"));
  }
}
