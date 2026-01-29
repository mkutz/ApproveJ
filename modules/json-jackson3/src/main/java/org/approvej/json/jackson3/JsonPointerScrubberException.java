package org.approvej.json.jackson3;

import org.jspecify.annotations.NullMarked;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;

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
