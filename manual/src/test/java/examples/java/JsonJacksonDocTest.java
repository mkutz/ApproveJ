package examples.java;

import static examples.ExampleClass.Tag.ENTERTAINMENT;
import static examples.ExampleClass.Tag.NEWS;
import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.json.jackson.JsonPointerScrubber.jsonPointer;
import static org.approvej.json.jackson.JsonPrettyPrinter.jsonPrettyPrinter;
import static org.approvej.verify.FileVerifier.file;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import examples.ExampleClass;
import java.util.List;
import org.junit.jupiter.api.Test;

class JsonJacksonDocTest {

  ExampleClass exampleObject = new ExampleClass();

  ObjectMapper jsonMapper = JsonMapper.builder().build();

  @Test
  void scrub_json_pointer() throws JsonProcessingException {
    // tag::scrub_json_pointer[]
    String createdBlogPostJson =
        exampleObject.createSomeTaggedBlogPost(
            "Latest News",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            List.of(NEWS, ENTERTAINMENT));

    approve(jsonMapper.readTree(createdBlogPostJson))
        .scrubbedOf(jsonPointer("/id").with("[scrubbed id]")) // <1>
        .scrubbedOf(jsonPointer("/published").with("[scrubbed published]")) // <2>
        .verify(file("json")); // <3>
    // end::scrub_json_pointer[]
  }

  @Test
  void pretty_print_json() throws JsonProcessingException {
    // tag::pretty_print_json[]
    String createdBlogPostJson =
        exampleObject.createSomeTaggedBlogPost(
            "Latest News",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            List.of(NEWS, ENTERTAINMENT));

    approve(jsonMapper.readTree(createdBlogPostJson))
        .scrubbedOf(jsonPointer("/id").with("[scrubbed id]"))
        .scrubbedOf(jsonPointer("/published").with("[scrubbed published]"))
        .printWith(jsonPrettyPrinter()) // <1>
        .verify(file("json"));
    // end::pretty_print_json[]
  }
}
