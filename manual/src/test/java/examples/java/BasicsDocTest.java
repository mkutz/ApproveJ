package examples.java;

import static examples.ExampleClass.createBlogPost;
import static examples.ExampleClass.createContact;
import static examples.ExampleClass.createPerson;
import static examples.ExampleClass.hello;
import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.approve.PathProviders.nextToTest;
import static org.approvej.print.ObjectPrinter.objectPrinter;
import static org.approvej.scrub.Scrubbers.dateTimeFormat;
import static org.approvej.scrub.Scrubbers.uuids;

import examples.ExampleClass.BlogPost;
import examples.ExampleClass.Contact;
import examples.ExampleClass.Person;
import java.time.LocalDate;
import org.approvej.print.Printer;
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
  void object_printer() {
    // tag::object_printer[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printWith(objectPrinter()) // <1>
        .byFile();
    // end::object_printer[]
  }

  @Test
  void custom_printer_function() {
    // tag::custom_printer_function[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printWith(it -> String.format("%s, born %s", it.name(), it.birthDate())) // <1>
        .byFile();
    // end::custom_printer_function[]
  }

  @Test
  void custom_printer() {
    // tag::custom_printer[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printWith(new PersonYamlPrinter()) // <1>
        .byFile();
    // end::custom_printer[]
  }

  @Test
  void scrubbing() {
    // tag::scrubbing[]
    BlogPost blogPost =
        createBlogPost("Latest News", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.");

    approve(blogPost)
        .printWith(objectPrinter())
        .scrubbedOf(dateTimeFormat()) // <1>
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
        .printWith(objectPrinter())
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
        .printWith(new PersonYamlPrinter()) // <1>
        .byFile(nextToTest().filenameExtension("yml"));
    // end::approve_file_custom_extension[]
  }

  @Test
  void approve_file_nextToTest_inSubdirectory() {
    // tag::approve_file_nextToTest_inSubdirectory[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person).printWith(new PersonYamlPrinter()).byFile(nextToTest().inSubdirectory());
    // end::approve_file_nextToTest_inSubdirectory[]
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
        .printWith(new PersonYamlPrinter())
        .byFile("src/test/resources/BasicExamples-approve_file_approved_path.yaml"); // <1>
    // end::approve_file_approved_path[]
  }

  // tag::person_yaml_printer[]
  public static class PersonYamlPrinter implements Printer<Person> {
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
