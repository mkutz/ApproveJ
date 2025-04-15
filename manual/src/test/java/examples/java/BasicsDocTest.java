package examples.java;

import static examples.ExampleClass.createBlogPost;
import static examples.ExampleClass.createContact;
import static examples.ExampleClass.createPerson;
import static examples.ExampleClass.hello;
import static examples.ExampleClass.personYamlPrinter;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.print.ObjectPrinter.objectPrinter;
import static org.approvej.scrub.Scrubbers.instants;
import static org.approvej.scrub.Scrubbers.uuids;
import static org.approvej.verify.BasePathProvider.approvedPath;
import static org.approvej.verify.DirectoryNextToTestPathProvider.directoryNextToTestAs;
import static org.approvej.verify.FileVerifier.inFile;
import static org.approvej.verify.InplaceVerifier.inplace;
import static org.approvej.verify.NextToTestPathProvider.nextToTestAs;

import examples.ExampleClass.BlogPost;
import examples.ExampleClass.Contact;
import examples.ExampleClass.Person;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class BasicsDocTest {

  @Test
  void approve_strings() {
    // tag::approve_strings[]
    String result = hello("World");

    approve(result) // <1>
        .verify(); // <2>
    // end::approve_strings[]
  }

  @Test
  void approve_pojos() {
    // tag::approve_pojos[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person) // <1>
        .verify(); // <2>
    // end::approve_pojos[]
  }

  @Test
  void object_printer() {
    // tag::object_printer[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printWith(objectPrinter()) // <1>
        .verify();
    // end::object_printer[]
  }

  @Test
  void custom_printer() {
    // tag::custom_printer[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printWith(it -> String.format("%s, born %s", it.name(), it.birthDate())) // <1>
        .verify();
    // end::custom_printer[]
  }

  @Test
  void scrubbing() {
    // tag::scrubbing[]
    BlogPost blogPost =
        createBlogPost("Latest News", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.");

    approve(blogPost)
        .printWith(objectPrinter())
        .scrubbedOf(instants(ISO_LOCAL_DATE_TIME)) // <1>
        .scrubbedOf(uuids()) // <2>
        .verify(); // <3>
    // end::scrubbing[]
  }

  @Test
  void custom_scrubbing() {
    // tag::custom_scrubbing[]
    Contact contact = createContact("Jane Doe", "jane@approvej.org", "+1 123 456 7890");
    approve(contact)
        .scrubbedOf(it -> new Contact(-1, it.name(), it.email(), it.phoneNumber())) // <1>
        .printWith(objectPrinter())
        .verify(); // <2>
    // end::custom_scrubbing[]
  }

  @Test
  void verify_file_next_to_test() {
    // tag::verify_file_next_to_test[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person).verify(inFile()); // <1>
    // end::verify_file_next_to_test[]
  }

  @Test
  void verify_file_next_to_test_as() {
    // tag::verify_file_next_to_test_as[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printWith(personYamlPrinter()) // <1>
        .verify(inFile(nextToTestAs("yaml"))); // <2>
    // end::verify_file_next_to_test_as[]
  }

  @Test
  void verify_file_directory_next_to_test_as() {
    // tag::verify_file_directory_next_to_test_as[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person).printWith(personYamlPrinter()).verify(inFile(directoryNextToTestAs("yaml")));
    // end::verify_file_directory_next_to_test_as[]
  }

  @Test
  void verify_inplace() {
    // tag::verify_inplace[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person).verify(inplace("Person[name=John Doe, birthDate=1990-01-01]"));
    // end::verify_inplace[]
  }

  @Test
  void verify_file_base_path() {
    // tag::verify_file_base_path[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printWith(personYamlPrinter())
        .verify(
            inFile(
                approvedPath(
                    "src/test/resources/BasicExamples-verify_file_base_path.yaml"))); // <1>
    // end::verify_file_base_path[]
  }
}
