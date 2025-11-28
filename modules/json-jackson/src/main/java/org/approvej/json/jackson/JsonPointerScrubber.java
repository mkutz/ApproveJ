package org.approvej.json.jackson;

import static org.approvej.scrub.Replacements.string;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import org.approvej.scrub.Scrubber;
import org.jspecify.annotations.NullMarked;

/** A {@link Scrubber} that scrubs a JSON node at a specific {@link JsonPointer} by replacing its */
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
