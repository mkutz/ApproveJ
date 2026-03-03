package examples.kotlin

import examples.ExampleClass.Contact
import examples.ExampleClass.createBlogPost
import examples.ExampleClass.createContact
import org.approvej.ApprovalBuilder.approve
import org.approvej.print.MultiLineStringPrintFormat.multiLineString
import org.approvej.scrub.Replacements.labeled
import org.approvej.scrub.Replacements.masking
import org.approvej.scrub.Scrubbers.dateTimeFormat
import org.approvej.scrub.Scrubbers.uuids
import org.junit.jupiter.api.Test

@org.approvej.ApprovalTest
class ScrubbingDocTest {

  @Test
  fun scrubbing() {
    // tag::scrubbing[]
    val blogPost =
      createBlogPost("Latest News", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.")

    approve(blogPost)
      .printedAs(multiLineString())
      .scrubbedOf(dateTimeFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")) // <1>
      .scrubbedOf(uuids()) // <2>
      .byFile()
    // end::scrubbing[]
  }

  @Test
  fun `replacement labeled`() {
    // tag::replacement_labeled[]
    val blogPost =
      createBlogPost("Latest News", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.")

    approve(blogPost)
      .printedAs(multiLineString())
      .scrubbedOf(uuids().replacement(labeled("id"))) // <1>
      .scrubbedOf(dateTimeFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").replacement(masking())) // <2>
      .byFile()
    // end::replacement_labeled[]
  }

  @Test
  fun `custom scrubbing`() {
    // tag::custom_scrubbing[]
    val contact = createContact("Jane Doe", "jane@approvej.org", "+1 123 456 7890")

    approve(contact)
      .scrubbedOf { Contact(-1, it.name, it.email, it.phoneNumber) } // <1>
      .printedAs(multiLineString())
      .byFile()
    // end::custom_scrubbing[]
  }
}
