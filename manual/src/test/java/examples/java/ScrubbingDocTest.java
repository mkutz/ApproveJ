package examples.java;

import static examples.ExampleClass.createBlogPost;
import static examples.ExampleClass.createContact;
import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.print.MultiLineStringPrintFormat.multiLineString;
import static org.approvej.scrub.Scrubbers.dateTimeFormat;
import static org.approvej.scrub.Scrubbers.uuids;

import examples.ExampleClass.Contact;
import org.junit.jupiter.api.Test;

@org.approvej.ApprovalTest
class ScrubbingDocTest {

  @Test
  void scrubbing() {
    // tag::scrubbing[]
    var blogPost =
        createBlogPost("Latest News", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.");

    approve(blogPost)
        .printedAs(multiLineString())
        .scrubbedOf(dateTimeFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")) // <1>
        .scrubbedOf(uuids()) // <2>
        .byFile(); // <3>
    // end::scrubbing[]
  }

  @Test
  void custom_scrubbing() {
    // tag::custom_scrubbing[]
    Contact contact = createContact("Jane Doe", "jane@approvej.org", "+1 123 456 7890");
    approve(contact)
        .scrubbedOf(it -> new Contact(-1, it.name(), it.email(), it.phoneNumber())) // <1>
        .printedAs(multiLineString())
        .byFile();
    // end::custom_scrubbing[]
  }
}
