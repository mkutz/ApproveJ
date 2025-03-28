package org.approvej.json.jackson;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.approvej.scrub.Scrubber;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class JsonPointerScrubber implements Scrubber<JsonNode> {

  private final JsonPointer jsonPointer;
  private final String replacement;

  public JsonPointerScrubber(JsonPointer jsonPointer, String replacement) {
    this.jsonPointer = jsonPointer;
    this.replacement = replacement;
  }

  public JsonPointerScrubber(JsonPointer jsonPointer) {
    this(jsonPointer, "[scrubbed]");
  }

  @Override
  public JsonNode apply(JsonNode unscrubbedJsonNode) {
    var scrubbedJsonNode = unscrubbedJsonNode.deepCopy();
    var parentPointer = JsonPointer.compile("/");
    var parentNode = (ObjectNode) scrubbedJsonNode.at(parentPointer);

    if (!parentNode.at(jsonPointer.last()).isMissingNode()) {
      parentNode.replace(jsonPointer.last().getMatchingProperty(), TextNode.valueOf(replacement));
    }

    return scrubbedJsonNode;
  }
}
