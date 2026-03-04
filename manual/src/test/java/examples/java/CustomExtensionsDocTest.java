package examples.java;

import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.scrub.Replacements.labeled;

import org.junit.jupiter.api.Test;

@org.approvej.ApprovalTest
class CustomExtensionsDocTest {

  @Test
  void custom_scrubber() {
    // tag::custom_scrubber[]
    approve("Contact jane@example.com or john@company.org for details.")
        .scrubbedOf(new EmailScrubber()) // <1>
        .byFile();
    // end::custom_scrubber[]
  }

  @Test
  void custom_scrubber_replacement() {
    // tag::custom_scrubber_replacement[]
    approve("Contact jane@example.com or john@company.org for details.")
        .scrubbedOf(new EmailScrubber().replacement(labeled("redacted"))) // <1>
        .byFile();
    // end::custom_scrubber_replacement[]
  }
}
