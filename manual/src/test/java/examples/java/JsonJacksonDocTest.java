package examples.java;

import static examples.ExampleClass.Tag.ENTERTAINMENT;
import static examples.ExampleClass.Tag.NEWS;
import static examples.ExampleClass.createTaggedBlogPost;
import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.approve.PathProviders.nextToTest;
import static org.approvej.approve.Verifiers.file;
import static org.approvej.json.jackson.JsonPointerScrubber.jsonPointer;
import static org.approvej.json.jackson.JsonPrettyPrinter.jsonPrettyPrinter;
import static org.approvej.json.jackson.JsonStringPrettyPrinter.jsonStringPrettyPrinter;
import static org.approvej.scrub.Scrubbers.instants;
import static org.approvej.scrub.Scrubbers.uuids;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.Test;

class JsonJacksonDocTest {

  ObjectMapper jsonMapper = JsonMapper.builder().build();

  @Test
  void scrub_json_pointer() throws JsonProcessingException {
    // tag::scrub_json_pointer[]
    String createdBlogPostJson =
        createTaggedBlogPost(
            "Latest News",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            List.of(NEWS, ENTERTAINMENT));

    approve(jsonMapper.readTree(createdBlogPostJson))
        .scrubbedOf(jsonPointer("/id").replacement("[scrubbed id]")) // <1>
        .scrubbedOf(jsonPointer("/published").replacement("[scrubbed published]")) // <2>
        .by(file(nextToTest().filenameExtension("json"))); // <3>
    // end::scrub_json_pointer[]
  }

  @Test
  void pretty_print_json() throws JsonProcessingException {
    // tag::pretty_print_json[]
    String createdBlogPostJson =
        createTaggedBlogPost(
            "Latest News",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            List.of(NEWS, ENTERTAINMENT));

    approve(jsonMapper.readTree(createdBlogPostJson))
        .scrubbedOf(jsonPointer("/id").replacement("[scrubbed id]"))
        .scrubbedOf(jsonPointer("/published").replacement("[scrubbed published]"))
        .printWith(jsonPrettyPrinter()) // <1>
        .byFile();
    // end::pretty_print_json[]
  }

  @Test
  void pretty_print_json_string() {
    // tag::pretty_print_json_string[]
    String createdBlogPostJson =
        createTaggedBlogPost(
            "Latest News",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            List.of(NEWS, ENTERTAINMENT));

    approve(createdBlogPostJson)
        .scrubbedOf(uuids())
        .scrubbedOf(instants(DateTimeFormatter.ISO_INSTANT))
        .printWith(jsonStringPrettyPrinter()) // <1>
        .byFile();
    // end::pretty_print_json_string[]
  }
}
