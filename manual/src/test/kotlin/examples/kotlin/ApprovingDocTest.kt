package examples.kotlin

import examples.ExampleClass.createPerson
import java.time.LocalDate
import org.approvej.ApprovalBuilder.approve
import org.approvej.approve.PathProviders.nextToTest
import org.approvej.approve.PathProviders.nextToTestInSubdirectory
import org.approvej.print.PrintFormat
import org.approvej.print.Printer
import org.junit.jupiter.api.Test

// tag::approval_test_annotation[]
@org.approvej.ApprovalTest
// end::approval_test_annotation[]
class ApprovingDocTest {

  @Test
  fun `approve inplace`() {
    // tag::approve_inplace[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person).byValue("Person[name=John Doe, birthDate=1990-01-01]")
    // end::approve_inplace[]
  }

  @Test
  fun `approve file next to test`() {
    // tag::approve_file_next_to_test[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person).byFile(nextToTest()) // <1>
    // end::approve_file_next_to_test[]
  }

  @Test
  fun `approve file custom extension`() {
    // tag::approve_file_custom_extension[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printedBy {
        """
        person:
          name: "${it.name}"
          birthDate: "${it.birthDate}"
        """
          .trimIndent()
      } // <1>
      .byFile(nextToTest().filenameExtension("yml")) // <2>
    // end::approve_file_custom_extension[]
  }

  @Test
  fun `approve file nextToTestInSubdirectory`() {
    // tag::approve_file_nextToTestInSubdirectory[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person).printedAs(PersonYamlPrinter()).byFile(nextToTestInSubdirectory())
    // end::approve_file_nextToTestInSubdirectory[]
  }

  @Test
  fun `approve file approved path`() {
    // tag::approve_file_approved_path[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printedAs(PersonYamlPrinter())
      .byFile("src/test/resources/BasicExamples-approve file approved path.yaml") // <1>
    // end::approve_file_approved_path[]
  }

  class PersonYamlPrinter : PrintFormat<examples.ExampleClass.Person> {
    override fun printer() =
      Printer<examples.ExampleClass.Person> { person ->
        """
      person:
        name: "${person.name}"
        birthDate: "${person.birthDate}"
      """
          .trimIndent()
      }

    override fun filenameExtension() = "yaml"
  }
}
