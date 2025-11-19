package org.approvej.json.jackson;

import static org.approvej.scrub.Replacements.string;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.approvej.scrub.Replacement;
import org.approvej.scrub.Scrubber;
import org.jspecify.annotations.NullMarked;

/** A {@link Scrubber} that scrubs a JSON node at a specific {@link JsonPointer} by replacing its */
@NullMarked
public class JsonPointerScrubber implements Scrubber<JsonPointerScrubber, JsonNode, String> {

  private final JsonPointer jsonPointer;
  private final Replacement<String> replacement;

  /**
   * Create a {@link JsonPointerScrubber} to replace the JSON node at the given {@link JsonPointer}
   * {@link String}.
   *
   * @param jsonPointerString a {@link String} compilable to a {@link JsonPointer} to be replaced
   * @return a new {@link JsonPointerScrubber}
   * @see JsonPointer#compile(String)
   */
  public static JsonPointerScrubber jsonPointer(String jsonPointerString) {
    return new JsonPointerScrubber(JsonPointer.compile(jsonPointerString), string("[scrubbed]"));
  }

  private JsonPointerScrubber(JsonPointer jsonPointer, Replacement<String> replacement) {
    this.jsonPointer = jsonPointer;
    this.replacement = replacement;
  }

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
    return new JsonPointerScrubber(jsonPointer, replacement);
  }
}
