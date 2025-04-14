package org.approvej.json.jackson;

import static org.approvej.scrub.Replacements.numbered;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.function.Function;
import org.approvej.scrub.Replacements;
import org.approvej.scrub.Scrubber;
import org.jspecify.annotations.NullMarked;

/** A {@link Scrubber} that scrubs a JSON node at a specific {@link JsonPointer} by replacing its */
@NullMarked
public class JsonPointerScrubber implements Scrubber<JsonNode> {

  private final JsonPointer jsonPointer;
  private final Function<Integer, Object> replacement;

  /**
   * Create a {@link JsonPointerScrubberBuilder} to replace the JSON node at the given {@link
   * JsonPointer} {@link String}.
   *
   * @param jsonPointerString a {@link String} compilable to a {@link JsonPointer} to be replaced
   * @return a new {@link JsonPointerScrubberBuilder}
   * @see JsonPointer#compile(String)
   */
  public static JsonPointerScrubberBuilder jsonPointer(String jsonPointerString) {
    return new JsonPointerScrubberBuilder(JsonPointer.compile(jsonPointerString));
  }

  private JsonPointerScrubber(JsonPointer jsonPointer, Function<Integer, Object> replacement) {
    this.jsonPointer = jsonPointer;
    this.replacement = replacement;
  }

  @Override
  public JsonNode apply(JsonNode unscrubbedJsonNode) {
    var scrubbedJsonNode = unscrubbedJsonNode.deepCopy();
    var parentPointer = JsonPointer.compile("");
    var parentNode = (ObjectNode) scrubbedJsonNode.at(parentPointer);

    if (!parentNode.at(jsonPointer.last()).isMissingNode()) {
      parentNode.replace(
          jsonPointer.last().getMatchingProperty(),
          TextNode.valueOf(replacement.apply(jsonPointer.getMatchingIndex()).toString()));
    }

    return scrubbedJsonNode;
  }

  /** Builder for creating a {@link JsonPointerScrubber}. */
  public static class JsonPointerScrubberBuilder {
    private final JsonPointer jsonPointer;

    private JsonPointerScrubberBuilder(JsonPointer jsonPointer) {
      this.jsonPointer = jsonPointer;
    }

    /**
     * Create a {@link Scrubber} to replace any match of the {@link #jsonPointer} with the result of
     * the given replacement {@link Function}.
     *
     * @param replacement a function that receives the finding index and returns the replacement
     *     string
     * @return a {@link JsonPointerScrubber} to replace any match of the {@link #jsonPointer} with
     *     the result of the given replacement {@link Function}.
     */
    public JsonPointerScrubber with(Function<Integer, Object> replacement) {
      return new JsonPointerScrubber(jsonPointer, replacement);
    }

    /**
     * Creates a new {@link Scrubber} to replace any match of the {@link #jsonPointer} with the
     * given static replacement.
     *
     * @param staticReplacement the static replacement {@link String}
     * @return a new {@link JsonPointerScrubber} to replace any match of the {@link #jsonPointer}
     *     with the given staticReplacement.
     */
    public JsonPointerScrubber with(String staticReplacement) {
      return new JsonPointerScrubber(jsonPointer, number -> staticReplacement);
    }

    /**
     * Creates a new {@link Scrubber} to replace strings matching the {@link #jsonPointer} with a
     * numbered replacement.
     *
     * @return a new {@link JsonPointerScrubber} using the {@link Replacements#numbered()}.
     */
    public JsonPointerScrubber withNumberedReplacement() {
      return new JsonPointerScrubber(jsonPointer, numbered());
    }
  }
}
