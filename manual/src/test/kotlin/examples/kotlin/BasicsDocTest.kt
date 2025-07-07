package examples.kotlin

import examples.ExampleClass.Contact
import examples.ExampleClass.Person
import examples.ExampleClass.createBlogPost
import examples.ExampleClass.createContact
import examples.ExampleClass.createPerson
import examples.ExampleClass.hello
import java.time.LocalDate
import org.approvej.ApprovalBuilder.approve
import org.approvej.approve.PathProviderBuilder.nextToTest
import org.approvej.approve.PathProviderBuilder.nextToTestInSubdirectory
import org.approvej.print.ObjectPrinter.objectPrinter
import org.approvej.print.Printer
import org.approvej.scrub.Scrubbers.dateTimeFormat
import org.approvej.scrub.Scrubbers.uuids
import org.assertj.core.api.Assumptions.assumeThat
import org.junit.jupiter.api.Test

class BasicsDocTest {

  @Test
  fun `approve strings`() {
    // tag::approve_strings[]
    val result = hello("World")

    approve(result) // <1>
      .byFile() // <2>
    // end::approve_strings[]
  }

  @Test
  fun `approve pojos`() {
    // tag::approve_pojos[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person) // <1>
      .byFile()
    // end::approve_pojos[]
  }

  @Test
  fun approve_named() {
    // tag::approve_named[]
    val jane = createPerson("Jane Doe", LocalDate.of(1990, 1, 1))
    val john = createPerson("John Doe", LocalDate.of(2012, 6, 2))

    approve(jane).named("jane").byFile()
    approve(john).named("john").byFile()
    // end::approve_named[]
  }

  @Test
  fun `object printer`() {
    // tag::object_printer[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printWith(objectPrinter()) // <1>
      .byFile()
    // end::object_printer[]
  }

  @Test
  fun `custom printer function`() {
    // tag::custom_printer_function[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printWith { "%s, born %s".format(it.name, it.birthDate) } // <1>
      .byFile()
    // end::custom_printer_function[]
  }

  @Test
  fun `custom printer`() {
    // tag::custom_printer[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printWith(PersonYamlPrinter()) // <1>
      .byFile()
    // end::custom_printer[]
  }

  @Test
  fun scrubbing() {
    // tag::scrubbing[]
    val blogPost =
      createBlogPost("Latest News", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.")

    approve(blogPost)
      .printWith(objectPrinter())
      .scrubbedOf(dateTimeFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")) // <1>
      .scrubbedOf(uuids()) // <2>
      .byFile()
    // end::scrubbing[]
  }

  @Test
  fun `custom scrubbing`() {
    // tag::custom_scrubbing[]
    val contact = createContact("Jane Doe", "jane@approvej.org", "+1 123 456 7890")

    approve(contact)
      .scrubbedOf { Contact(-1, it.name, it.email, it.phoneNumber) } // <1>
      .printWith(objectPrinter())
      .byFile()
    // end::custom_scrubbing[]
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
      .printWith {
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

    approve(person).printWith(PersonYamlPrinter()).byFile(nextToTestInSubdirectory())
    // end::approve_file_nextToTestInSubdirectory[]
  }

  @Test
  fun `approve inplace`() {
    // tag::approve_inplace[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person).byValue("Person[name=John Doe, birthDate=1990-01-01]")
    // end::approve_inplace[]
  }

  @Test
  fun `approve file approved path`() {
    // tag::approve_file_approved_path[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printWith(PersonYamlPrinter())
      .byFile("src/test/resources/BasicExamples-approve file approved path.yaml") // <1>
    // end::approve_file_approved_path[]
  }

  @Test
  fun `approve reviewWith fileReviewer`() {
    assumeThat(ProcessBuilder("which", "meld").start().waitFor()).isEqualTo(0)
    // tag::approve_reviewWith_fileReviewer[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printWith(PersonYamlPrinter())
      .reviewWith("meld \"{receivedFile}\" \"{approvedFile}\"") // <1>
      .byFile() // <2>
    // end::approve_reviewWith_fileReviewer[]
  }

  // tag::person_yaml_printer[]
  class PersonYamlPrinter : Printer<Person> {
    override fun apply(person: Person) =
      """
        person:
          name: "${person.name}"
          birthDate: "${person.birthDate}"
        """
        .trimIndent()

    override fun filenameExtension() = "yaml"
  }
  // end::person_yaml_printer[]
}
