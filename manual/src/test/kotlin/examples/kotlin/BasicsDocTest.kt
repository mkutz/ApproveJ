package examples.kotlin

import examples.ExampleClass.*
import java.time.LocalDate
import org.approvej.ApprovalBuilder.approve
import org.approvej.approve.PathProviders.nextToTest
import org.approvej.print.ObjectPrinter.objectPrinter
import org.approvej.print.Printer
import org.approvej.scrub.Scrubbers.instants
import org.approvej.scrub.Scrubbers.uuids
import org.junit.jupiter.api.Test

class BasicsDocTest {

  @Test
  fun approve_strings() {
    // tag::approve_strings[]
    val result = hello("World")

    approve(result) // <1>
      .byFile() // <2>
    // end::approve_strings[]
  }

  @Test
  fun approve_pojos() {
    // tag::approve_pojos[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person) // <1>
      .byFile()
    // end::approve_pojos[]
  }

  @Test
  fun object_printer() {
    // tag::object_printer[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printWith(objectPrinter()) // <1>
      .byFile()
    // end::object_printer[]
  }

  @Test
  fun custom_printer_function() {
    // tag::custom_printer_function[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printWith { "%s, born %s".format(it.name, it.birthDate) } // <1>
      .byFile()
    // end::custom_printer_function[]
  }

  @Test
  fun custom_printer() {
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
      .scrubbedOf(instants()) // <1>
      .scrubbedOf(uuids()) // <2>
      .byFile()
    // end::scrubbing[]
  }

  @Test
  fun custom_scrubbing() {
    // tag::custom_scrubbing[]
    val contact = createContact("Jane Doe", "jane@approvej.org", "+1 123 456 7890")

    approve(contact)
      .scrubbedOf { Contact(-1, it.name, it.email, it.phoneNumber) } // <1>
      .printWith(objectPrinter())
      .byFile()
    // end::custom_scrubbing[]
  }

  @Test
  fun approve_file_next_to_test() {
    // tag::approve_file_next_to_test[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person).byFile(nextToTest()) // <1>
    // end::approve_file_next_to_test[]
  }

  @Test
  fun approve_file_custom_extension() {
    // tag::approve_file_custom_extension[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printWith(PersonYamlPrinter()) // <1>
      .byFile(nextToTest().filenameExtension("yml")) // <2>
    // end::approve_file_custom_extension[]
  }

  @Test
  fun approve_file_nextToTest_inSubdirectory() {
    // tag::approve_file_nextToTest_inSubdirectory[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printWith(PersonYamlPrinter())
      .byFile(nextToTest().inSubdirectory().filenameExtension("yaml"))
    // end::approve_file_nextToTest_inSubdirectory[]
  }

  @Test
  fun approve_inplace() {
    // tag::approve_inplace[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person).byValue("Person[name=John Doe, birthDate=1990-01-01]")
    // end::approve_inplace[]
  }

  @Test
  fun approve_file_approved_path() {
    // tag::approve_file_approved_path[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printWith(PersonYamlPrinter())
      .byFile("src/test/resources/BasicExamples-approve_file_approved_path.yaml") // <1>
    // end::approve_file_approved_path[]
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
