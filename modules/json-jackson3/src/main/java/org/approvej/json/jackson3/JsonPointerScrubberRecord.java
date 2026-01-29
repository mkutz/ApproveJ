package org.approvej.json.jackson3;

import org.approvej.scrub.Replacement;
import org.jspecify.annotations.NullMarked;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

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
        StringNode replacementNode =
            StringNode.valueOf(replacement.apply(propertyName, jsonPointer.getMatchingIndex()));
        objectNode.replace(propertyName, replacementNode);
      }
      case ArrayNode arrayNode -> {
        int index = jsonPointer.last().getMatchingIndex();
        StringNode replacementNode =
            StringNode.valueOf(
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
