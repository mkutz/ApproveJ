package examples.kotlin

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.json.JsonMapper
import examples.ExampleClass.Tag.ENTERTAINMENT
import examples.ExampleClass.Tag.NEWS
import examples.ExampleClass.createTaggedBlogPost
import org.approvej.ApprovalBuilder.approve
import org.approvej.approve.Approvers.file
import org.approvej.approve.PathProviders.nextToTest
import org.approvej.json.jackson.JsonPointerScrubber.jsonPointer
import org.approvej.json.jackson.JsonPrintFormat.json
import org.approvej.scrub.Scrubbers.dateTimeFormat
import org.approvej.scrub.Scrubbers.uuids
import org.junit.jupiter.api.Test

class JsonJacksonDocTest {

  private val jsonMapper = JsonMapper.builder().build()

  @Test
  @Throws(JsonProcessingException::class)
  fun `scrub json pointer`() {
    // tag::scrub_json_pointer[]
    val createdBlogPostJson =
      createTaggedBlogPost(
        "Latest News",
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
        listOf(NEWS, ENTERTAINMENT),
      )

    approve(jsonMapper.readTree(createdBlogPostJson))
      .scrubbedOf(jsonPointer("/id").replacement("[scrubbed id]")) // <1>
      .scrubbedOf(jsonPointer("/published").replacement("[scrubbed published]")) // <2>
      .by(file(nextToTest().filenameExtension("json"))) // <3>
    // end::scrub_json_pointer[]
  }

  @Test
  @Throws(JsonProcessingException::class)
  fun `pretty print json`() {
    // tag::pretty_print_json[]
    val createdBlogPostJson =
      createTaggedBlogPost(
        "Latest News",
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
        listOf(NEWS, ENTERTAINMENT),
      )

    approve(jsonMapper.readTree(createdBlogPostJson))
      .scrubbedOf(jsonPointer("/id").replacement("[scrubbed id]"))
      .scrubbedOf(jsonPointer("/published").replacement("[scrubbed published]"))
      .printedAs(json()) // <1>
      .byFile()
    // end::pretty_print_json[]
  }

  @Test
  fun `pretty print json string`() {
    // tag::pretty_print_json_string[]
    val createdBlogPostJson =
      createTaggedBlogPost(
        "Latest News",
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
        listOf(NEWS, ENTERTAINMENT),
      )

    approve(createdBlogPostJson)
      .scrubbedOf(uuids())
      .scrubbedOf(dateTimeFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX"))
      .printedAs(json()) // <1>
      .byFile()
    // end::pretty_print_json_string[]
  }
}
