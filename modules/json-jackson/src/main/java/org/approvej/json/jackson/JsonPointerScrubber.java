package org.approvej.json.jackson;

import static org.approvej.scrub.Replacements.string;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.function.Function;
import org.approvej.scrub.Scrubber;
import org.jspecify.annotations.NullMarked;

/** A {@link Scrubber} that scrubs a JSON node at a specific {@link JsonPointer} by replacing its */
@NullMarked
public class JsonPointerScrubber implements Scrubber<JsonNode> {

  private final JsonPointer jsonPointer;
  private Function<Integer, Object> replacement;

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

  private JsonPointerScrubber(JsonPointer jsonPointer, Function<Integer, Object> replacement) {
    this.jsonPointer = jsonPointer;
    this.replacement = replacement;
  }

  @Override
  public JsonNode apply(JsonNode unscrubbedJsonNode) {
    JsonNode scrubbedJsonNode = unscrubbedJsonNode.deepCopy();
    JsonPointer parentPointer = JsonPointer.compile("");
    ObjectNode parentNode = (ObjectNode) scrubbedJsonNode.at(parentPointer);

    if (!parentNode.at(jsonPointer.last()).isMissingNode()) {
      parentNode.replace(
          jsonPointer.last().getMatchingProperty(),
          TextNode.valueOf(replacement.apply(jsonPointer.getMatchingIndex()).toString()));
    }

    return scrubbedJsonNode;
  }

  /**
   * Create a {@link Scrubber} to replace any match of the {@link #jsonPointer} with the result of
   * the given replacement {@link Function}.
   *
   * @param replacement a function that receives the finding index and returns the replacement
   *     string
   * @return a {@link JsonPointerScrubber} to replace any match of the {@link #jsonPointer} with the
   *     result of the given replacement {@link Function}.
   */
  public JsonPointerScrubber replacement(Function<Integer, Object> replacement) {
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
    this.replacement = number -> staticReplacement;
    return this;
  }
}
