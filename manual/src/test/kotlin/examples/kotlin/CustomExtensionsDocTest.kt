package examples.kotlin

import org.approvej.ApprovalBuilder.approve
import org.approvej.scrub.Replacements.labeled
import org.junit.jupiter.api.Test

@org.approvej.ApprovalTest
class CustomExtensionsDocTest {

  @Test
  fun `custom scrubber`() {
    // tag::custom_scrubber[]
    approve("Contact jane@example.com or john@company.org for details.")
      .scrubbedOf(EmailScrubber()) // <1>
      .byFile()
    // end::custom_scrubber[]
  }

  @Test
  fun `custom scrubber replacement`() {
    // tag::custom_scrubber_replacement[]
    approve("Contact jane@example.com or john@company.org for details.")
      .scrubbedOf(EmailScrubber().replacement(labeled("redacted"))) // <1>
      .byFile()
    // end::custom_scrubber_replacement[]
  }
}
