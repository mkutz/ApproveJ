package examples.kotlin

import examples.ExampleClass.Contact
import examples.ExampleClass.createBlogPost
import examples.ExampleClass.createContact
import examples.ExampleClass.createPerson
import examples.ExampleClass.hello
import examples.ExampleClass.personYamlPrinter
import org.approvej.ApprovalBuilder.approve
import org.approvej.print.ObjectPrinter.objectPrinter
import org.approvej.scrub.InstantScrubber.instants
import org.approvej.scrub.UuidScrubber.uuids
import org.approvej.verify.BasePathProvider.approvedPath
import org.approvej.verify.DirectoryNextToTestPathProvider.directoryNextToTestAs
import org.approvej.verify.FileVerifier.inFile
import org.approvej.verify.InplaceVerifier.inplace
import org.approvej.verify.NextToTestPathProvider.nextToTestAs
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

class BasicsDocTest {

  @Test
  fun approve_strings() {
    // tag::approve_strings[]
    val result = hello("World")

    approve(result) // <1>
      .verify() // <2>
    // end::approve_strings[]
  }

  @Test
  fun approve_pojos() {
    // tag::approve_pojos[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person) // <1>
      .verify() // <2>
    // end::approve_pojos[]
  }

  @Test
  fun object_printer() {
    // tag::object_printer[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printWith(objectPrinter()) // <1>
      .verify()
    // end::object_printer[]
  }

  @Test
  fun custom_printer() {
    // tag::custom_printer[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printWith { "%s, born %s".format(it.name, it.birthDate) } // <1>
      .verify()
    // end::custom_printer[]
  }

  @Test
  fun scrubbing() {
    // tag::scrubbing[]
    val blogPost = createBlogPost("Latest News", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.")

    approve(blogPost)
      .printWith(objectPrinter())
      .scrubbedOf(instants(ISO_LOCAL_DATE_TIME)) // <1>
      .scrubbedOf(uuids()) // <2>
      .verify()
    // end::scrubbing[]
  }

  @Test
  fun custom_scrubbing() {
    // tag::custom_scrubbing[]
    val contact = createContact("Jane Doe", "jane@approvej.org", "+1 123 456 7890")

    approve(contact)
      .scrubbedOf { Contact(-1, it.name, it.email, it.phoneNumber) } // <1>
      .printWith(objectPrinter())
      .verify() // <2>
    // end::custom_scrubbing[]
  }

  @Test
  fun verify_file_next_to_test() {
    // tag::verify_file_next_to_test[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .verify(inFile()) // <1>
    // end::verify_file_next_to_test[]
  }

  @Test
  fun verify_file_next_to_test_as() {
    // tag::verify_file_next_to_test_as[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printWith(personYamlPrinter()) // <1>
      .verify(inFile(nextToTestAs("yaml"))) // <2>
    // end::verify_file_next_to_test_as[]
  }

  @Test
  fun verify_file_directory_next_to_test_as() {
    // tag::verify_file_directory_next_to_test_as[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printWith(personYamlPrinter())
      .verify(inFile(directoryNextToTestAs("yaml")))
    // end::verify_file_directory_next_to_test_as[]
  }

  @Test
  fun verify_inplace() {
    // tag::verify_inplace[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .verify(inplace("Person[name=John Doe, birthDate=1990-01-01]"))
    // end::verify_inplace[]
  }

  @Test
  fun verify_file_base_path() {
    // tag::verify_file_base_path[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printWith(personYamlPrinter())
      .verify(inFile(approvedPath("src/test/resources/BasicExamples-verify_file_base_path.yaml"))) // <1>
    // end::verify_file_base_path[]
  }
}
