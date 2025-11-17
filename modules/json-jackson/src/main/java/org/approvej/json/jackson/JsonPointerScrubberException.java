package org.approvej.json.jackson;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import org.jspecify.annotations.NullMarked;

/** Exception thrown when scrubbing a JSON node defined by {@link JsonPointer} fails. */
@NullMarked
class JsonPointerScrubberException extends RuntimeException {

  /**
   * Creates a new JsonPointerScrubberException.
   *
   * @param jsonPointer the full pointer that should have been scrubbed
   * @param parentNode the actually found parent node (if any)
   */
  public JsonPointerScrubberException(JsonPointer jsonPointer, JsonNode parentNode) {
    super(
        "Failed to scrub JSON pointer %s, parent node %s"
            .formatted(jsonPointer.toString(), parentNode));
  }
}
