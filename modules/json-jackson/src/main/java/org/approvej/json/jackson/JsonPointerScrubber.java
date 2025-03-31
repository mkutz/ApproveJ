package org.approvej.json.jackson;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.approvej.scrub.Scrubber;
import org.jspecify.annotations.NullMarked;

/** A {@link Scrubber} that scrubs a JSON node at a specific {@link JsonPointer} by replacing its */
@NullMarked
public class JsonPointerScrubber implements Scrubber<JsonNode> {

  private static final String DEFAULT_REPLACEMENT = "[scrubbed]";
  private final JsonPointer jsonPointer;
  private final String replacement;

  /**
   * Create a {@link JsonPointerScrubber} that replaces the JSON node at the given {@link
   * JsonPointer} with the given replacement {@link String}.
   *
   * @param jsonPointer the {@link JsonPointer} to be replaced
   * @param replacement the replacement {@link String}
   */
  public JsonPointerScrubber(JsonPointer jsonPointer, String replacement) {
    this.jsonPointer = jsonPointer;
    this.replacement = replacement;
  }

  /**
   * Create a {@link JsonPointerScrubber} that replaces the JSON node at the given {@link
   * JsonPointer} with {@value #DEFAULT_REPLACEMENT}.
   *
   * @param jsonPointer the {@link JsonPointer} to be replaced
   */
  public JsonPointerScrubber(JsonPointer jsonPointer) {
    this(jsonPointer, DEFAULT_REPLACEMENT);
  }

  @Override
  public JsonNode apply(JsonNode unscrubbedJsonNode) {
    var scrubbedJsonNode = unscrubbedJsonNode.deepCopy();
    var parentPointer = JsonPointer.compile("");
    var parentNode = (ObjectNode) scrubbedJsonNode.at(parentPointer);

    if (!parentNode.at(jsonPointer.last()).isMissingNode()) {
      parentNode.replace(jsonPointer.last().getMatchingProperty(), TextNode.valueOf(replacement));
    }

    return scrubbedJsonNode;
  }
}
