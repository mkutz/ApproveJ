package examples.java;

import static examples.ExampleClass.createPerson;
import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.approve.PathProviders.nextToTest;
import static org.approvej.approve.PathProviders.nextToTestInSubdirectory;

import examples.ExampleClass.Person;
import examples.PersonYamlPrintFormat;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

// tag::approval_test_annotation[]
@org.approvej.ApprovalTest
// end::approval_test_annotation[]
class ApprovingDocTest {

  @Test
  void approve_inplace() {
    // tag::approve_inplace[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person).byValue("Person[name=John Doe, birthDate=1990-01-01]");
    // end::approve_inplace[]
  }

  @Test
  void approve_inplace_auto_update() {
    // tag::approve_inplace_auto_update[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .byValue(
            """
            Person[name=John Doe, birthDate=1990-01-01]\
            """);
    // end::approve_inplace_auto_update[]
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
        .printedBy(
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
  void approve_file_approved_path() {
    // tag::approve_file_approved_path[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printedAs(new PersonYamlPrintFormat())
        .byFile("src/test/resources/BasicExamples-approve file approved path.yaml"); // <1>
    // end::approve_file_approved_path[]
  }
}
