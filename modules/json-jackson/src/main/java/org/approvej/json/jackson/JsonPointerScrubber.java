package org.approvej.json.jackson;

import static org.approvej.scrub.Replacements.string;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.function.Function;
import org.approvej.scrub.Replacement;
import org.approvej.scrub.Scrubber;
import org.jspecify.annotations.NullMarked;

/** A {@link Scrubber} that scrubs a JSON node at a specific {@link JsonPointer} by replacing its */
@NullMarked
public class JsonPointerScrubber implements Scrubber<JsonNode> {

  private final JsonPointer jsonPointer;
  private Replacement replacement;

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

  private JsonPointerScrubber(JsonPointer jsonPointer, Replacement replacement) {
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
        arrayNode.insert(index, replacementNode);
      }
      default -> throw new JsonPointerScrubberException(jsonPointer, parentNode);
    }

    return scrubbedJsonNode;
  }

  /**
   * Create a {@link Scrubber} to replace any match of the {@link #jsonPointer} with the result of
   * the given replacement {@link Function}.
   *
   * @param replacement the {@link Replacement} function
   * @return a {@link JsonPointerScrubber} to replace any match of the {@link #jsonPointer} with the
   *     result of the given replacement {@link Function}.
   */
  public JsonPointerScrubber replacement(Replacement replacement) {
    this.replacement = replacement;
    return this;
  }

  /**
   * Creates a new {@link Scrubber} to replace any match of the {@link #jsonPointer} with the given
   * static replacement.
   *
   * @param staticReplacement the static replacement {@link String}
   * @return a new {@link JsonPointerScrubber} to replace any match of the {@link #jsonPointer} with
   *     the given staticReplacement.
   */
  public JsonPointerScrubber replacement(String staticReplacement) {
    this.replacement = (match, number) -> staticReplacement;
    return this;
  }
}
