package examples.kotlin

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.json.JsonMapper
import examples.ExampleClass.Tag.ENTERTAINMENT
import examples.ExampleClass.Tag.NEWS
import examples.ExampleClass.createTaggedBlogPost
import org.approvej.ApprovalBuilder.approve
import org.approvej.json.jackson.JsonPointerScrubber.jsonPointer
import org.approvej.json.jackson.JsonPrettyPrinter.jsonPrettyPrinter
import org.approvej.json.jackson.JsonStringPrettyPrinter.jsonStringPrettyPrinter
import org.approvej.scrub.InstantScrubber.instants
import org.approvej.scrub.UuidScrubber.uuids
import org.approvej.verify.FileVerifier.file
import org.junit.jupiter.api.Test
import java.time.format.DateTimeFormatter

class JsonJacksonDocTest {

  private val jsonMapper = JsonMapper.builder().build()

  @Test
  @Throws(JsonProcessingException::class)
  fun scrub_json_pointer() {
    // tag::scrub_json_pointer[]
    val createdBlogPostJson = createTaggedBlogPost(
      "Latest News",
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
      listOf(NEWS, ENTERTAINMENT)
    )

    approve(jsonMapper.readTree(createdBlogPostJson))
      .scrubbedOf(jsonPointer("/id").with("[scrubbed id]")) // <1>
      .scrubbedOf(jsonPointer("/published").with("[scrubbed published]")) // <2>
      .verify(file("json")) // <3>
    // end::scrub_json_pointer[]
  }

  @Test
  @Throws(JsonProcessingException::class)
  fun pretty_print_json() {
    // tag::pretty_print_json[]
    val createdBlogPostJson = createTaggedBlogPost(
      "Latest News",
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
      listOf(NEWS, ENTERTAINMENT)
    )

    approve(jsonMapper.readTree(createdBlogPostJson))
      .scrubbedOf(jsonPointer("/id").with("[scrubbed id]"))
      .scrubbedOf(jsonPointer("/published").with("[scrubbed published]"))
      .printWith(jsonPrettyPrinter()) // <1>
      .verify(file("json"))
    // end::pretty_print_json[]
  }

  @Test
  fun pretty_print_json_string() {
    // tag::pretty_print_json_string[]
    val createdBlogPostJson = createTaggedBlogPost(
      "Latest News",
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
      listOf(NEWS, ENTERTAINMENT)
    )

    approve(createdBlogPostJson)
      .scrubbedOf(uuids())
      .scrubbedOf(instants(DateTimeFormatter.ISO_INSTANT))
      .printWith(jsonStringPrettyPrinter()) // <1>
      .verify(file("json"))
    // end::pretty_print_json_string[]
  }
}
