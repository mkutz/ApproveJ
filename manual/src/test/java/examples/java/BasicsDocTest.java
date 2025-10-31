package examples.java;

import static examples.ExampleClass.createBlogPost;
import static examples.ExampleClass.createContact;
import static examples.ExampleClass.createPerson;
import static examples.ExampleClass.hello;
import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.approve.PathProviderBuilder.nextToTest;
import static org.approvej.approve.PathProviderBuilder.nextToTestInSubdirectory;
import static org.approvej.print.MultiLineStringFormat.multiLineString;
import static org.approvej.scrub.Scrubbers.dateTimeFormat;
import static org.approvej.scrub.Scrubbers.uuids;
import static org.assertj.core.api.Assumptions.assumeThat;

import examples.ExampleClass.BlogPost;
import examples.ExampleClass.Contact;
import examples.ExampleClass.Person;
import java.io.IOException;
import java.time.LocalDate;
import org.approvej.print.PrintFormat;
import org.junit.jupiter.api.Test;

class BasicsDocTest {

  @Test
  void approve_strings() {
    // tag::approve_strings[]
    String result = hello("World");

    approve(result) // <1>
        .byFile(); // <2>
    // end::approve_strings[]
  }

  @Test
  void approve_pojos() {
    // tag::approve_pojos[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person) // <1>
        .byFile();
    // end::approve_pojos[]
  }

  @Test
  void approve_named() {
    // tag::approve_named[]
    Person jane = createPerson("Jane Doe", LocalDate.of(1990, 1, 1));
    Person john = createPerson("John Doe", LocalDate.of(2012, 6, 2));

    approve(jane).named("jane").byFile();
    approve(john).named("john").byFile();
    // end::approve_named[]
  }

  @Test
  void multi_line_string_printer() {
    // tag::multi_line_string_printer[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printedAs(multiLineString()) // <1>
        .byFile();
    // end::multi_line_string_printer[]
  }

  @Test
  void custom_printer_function() {
    // tag::custom_printer_function[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printedAs(it -> String.format("%s, born %s", it.name(), it.birthDate())) // <1>
        .byFile();
    // end::custom_printer_function[]
  }

  @Test
  void custom_printer() {
    // tag::custom_printer[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printedAs(new PersonYamlPrintFormat()) // <1>
        .byFile();
    // end::custom_printer[]
  }

  @Test
  void scrubbing() {
    // tag::scrubbing[]
    BlogPost blogPost =
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

  @Test
  void approve_file_next_to_test() {
    // tag::approve_file_next_to_test[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person).byFile(nextToTest()); // <1>
    // end::approve_file_next_to_test[]
  }

  @Test
  void approve_file_custom_extension() {
    // tag::approve_file_custom_extension[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printedAs(
            it ->
                """
                person:
                  name: "%s"
                  birthDate: "%s"
                """
                    .formatted(it.name(), it.birthDate())) // <1>
        .byFile(nextToTest().filenameExtension("yml"));
    // end::approve_file_custom_extension[]
  }

  @Test
  void approve_file_nextToTestInSubdirectory() {
    // tag::approve_file_nextToTestInSubdirectory[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person).printedAs(new PersonYamlPrintFormat()).byFile(nextToTestInSubdirectory());
    // end::approve_file_nextToTestInSubdirectory[]
  }

  @Test
  void approve_inplace() {
    // tag::approve_inplace[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person).byValue("Person[name=John Doe, birthDate=1990-01-01]");
    // end::approve_inplace[]
  }

  @Test
  void approve_file_approved_path() {
    // tag::approve_file_approved_path[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printedAs(new PersonYamlPrintFormat())
        .byFile("src/test/resources/BasicExamples-approve file approved path.yaml"); // <1>
    // end::approve_file_approved_path[]
  }

  @Test
  void approve_reviewWith_fileReviewer() throws IOException, InterruptedException {
    assumeThat(new ProcessBuilder("which", "meld").start().waitFor()).isEqualTo(0);
    // tag::approve_reviewWith_fileReviewer[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printedAs(new PersonYamlPrintFormat())
        .reviewedWith("idea diff {receivedFile} {approvedFile}") // <1>
        .byFile(); // <2>
    // end::approve_reviewWith_fileReviewer[]
  }

  // tag::person_yaml_printer[]
  public static class PersonYamlPrintFormat implements PrintFormat<Person> {
    @Override
    public String apply(Person person) {
      return """
             person:
               name: "%s"
               birthDate: "%s"
             """
          .formatted(person.name(), person.birthDate());
    }

    @Override
    public String filenameExtension() {
      return "yaml";
    }
  }
  // end::person_yaml_printer[]
}
