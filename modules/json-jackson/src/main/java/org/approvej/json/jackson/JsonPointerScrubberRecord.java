package org.approvej.json.jackson;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.approvej.scrub.Replacement;
import org.jspecify.annotations.NullMarked;

@NullMarked
record JsonPointerScrubberRecord(JsonPointer jsonPointer, Replacement<String> replacement)
    implements JsonPointerScrubber {

  @Override
  public JsonNode apply(JsonNode unscrubbedJsonNode) {
    if (unscrubbedJsonNode.at(jsonPointer).isMissingNode()) {
      return unscrubbedJsonNode;
    }

    JsonNode scrubbedJsonNode = unscrubbedJsonNode.deepCopy();
    JsonNode parentNode = scrubbedJsonNode.at(jsonPointer.head());
    switch (parentNode) {
      case ObjectNode objectNode -> {
        String propertyName = jsonPointer.last().getMatchingProperty();
        TextNode replacementNode =
            TextNode.valueOf(replacement.apply(propertyName, jsonPointer.getMatchingIndex()));
        objectNode.replace(propertyName, replacementNode);
      }
      case ArrayNode arrayNode -> {
        int index = jsonPointer.last().getMatchingIndex();
        TextNode replacementNode =
            TextNode.valueOf(
                replacement.apply(String.valueOf(index), jsonPointer.getMatchingIndex()));
        arrayNode.set(index, replacementNode);
      }
      default -> throw new JsonPointerScrubberException(jsonPointer, parentNode);
    }

    return scrubbedJsonNode;
  }

  @Override
  public JsonPointerScrubber replacement(Replacement<String> replacement) {
    return new JsonPointerScrubberRecord(jsonPointer, replacement);
  }
}
