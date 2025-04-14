package examples.kotlin

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.json.JsonMapper
import examples.ExampleClass
import examples.ExampleClass.Tag.ENTERTAINMENT
import examples.ExampleClass.Tag.NEWS
import org.approvej.ApprovalBuilder.approve
import org.approvej.json.jackson.JsonPointerScrubber.jsonPointer
import org.approvej.json.jackson.JsonPrettyPrinter.jsonPrettyPrinter
import org.approvej.verify.FileVerifier.file
import org.junit.jupiter.api.Test

class JsonJacksonDocTest {

  private val exampleObject = ExampleClass()
  private val jsonMapper = JsonMapper.builder().build()

  @Test
  @Throws(JsonProcessingException::class)
  fun scrub_json_pointer() {
    // tag::scrub_json_pointer[]
    val createdBlogPostJson = exampleObject.createSomeTaggedBlogPost(
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
    val createdBlogPostJson = exampleObject.createSomeTaggedBlogPost(
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
}
