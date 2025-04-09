package examples.java;

import static examples.ExampleClass.personYamlPrinter;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.print.ObjectPrinter.objectPrinter;
import static org.approvej.scrub.InstantScrubber.instants;
import static org.approvej.scrub.UuidScrubber.uuids;
import static org.approvej.verify.FileVerifier.BasePathProvider.approvedPath;
import static org.approvej.verify.FileVerifier.file;
import static org.approvej.verify.InplaceVerifier.inplace;

import examples.ExampleClass;
import examples.ExampleClass.Contact;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class BasicExamples {

  ExampleClass exampleObject = new ExampleClass();

  @Test
  void approve_strings() {
    // tag::approve_strings[]
    String result = exampleObject.createSomeSimpleString();
    approve(result) // <1>
        .verify(); // <2>
    // end::approve_strings[]
  }

  @Test
  void approve_pojos() {
    // tag::approve_pojos[]
    approve(exampleObject.createSomePerson()) // <1>
        .verify(); // <2>
    // end::approve_pojos[]
  }

  @Test
  void object_printer() {
    // tag::object_printer[]
    approve(exampleObject.createSomePerson()) // <1>
        .printWith(objectPrinter()) // <2>
        .verify();
    // end::object_printer[]
  }

  @Test
  void custom_printer() {
    // tag::custom_printer[]
    approve(exampleObject.createSomePerson()) // <1>
        .printWith(person -> String.format("%s, born %s", person.name(), person.birthDate())) // <2>
        .verify();
    // end::custom_printer[]
  }

  @Test
  void scrubbing() {
    // tag::scrubbing[]
    approve(
            exampleObject.createSomeBlogPost(
                "Latest News", "Lorem ipsum dolor sit amet, consectetur adipiscing elit."))
        .printWith(objectPrinter())
        .scrubbedOf(instants(ISO_LOCAL_DATE_TIME)) // <1>
        .scrubbedOf(uuids()) // <2>
        .verify(); // <3>
    // end::scrubbing[]
  }

  @Test
  void custom_scrubbing() {
    // tag::custom_scrubbing[]
    approve(exampleObject.createContact("Jane Doe", "jane@approvej.org", "+1 123 456 7890"))
        .scrubbedOf(
            contact ->
                new Contact(-1, contact.name(), contact.email(), contact.phoneNumber())) // <1>
        .printWith(objectPrinter())
        .verify(); // <2>
    // end::custom_scrubbing[]
  }

  @Test
  void verify_inplace() {
    // tag::verify_inplace[]
    approve(exampleObject.createSomePerson()) // <1>
        .verify(inplace("Person[name=John Doe, birthDate=1990-01-01]")); // <2>
    // end::verify_inplace[]
  }

  @Test
  void verify_file_base_path() {
    // tag::verify_file_base_path[]
    approve(exampleObject.createSomePerson())
        .printWith(personYamlPrinter())
        .verify(
            file(
                approvedPath(
                    Path.of(
                        "src/test/resources/BasicExamples-verify_file_base_path.yaml")))); // <1>
    // end::verify_file_base_path[]
  }
}
