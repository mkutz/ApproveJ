package examples.kotlin

import examples.ExampleClass
import examples.ExampleClass.Contact
import examples.ExampleClass.personYamlPrinter
import org.approvej.ApprovalBuilder.approve
import org.approvej.print.ObjectPrinter.objectPrinter
import org.approvej.scrub.InstantScrubber.instants
import org.approvej.scrub.UuidScrubber.uuids
import org.approvej.verify.FileVerifier.BasePathProvider.approvedPath
import org.approvej.verify.FileVerifier.file
import org.approvej.verify.InplaceVerifier.inplace
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

class BasicsDocTest {

  val exampleObject = ExampleClass()

  @Test
  fun approve_strings() {
    // tag::approve_strings[]
    val result = exampleObject.createSomeSimpleString()

    approve(result) // <1>
      .verify() // <2>
    // end::approve_strings[]
  }

  @Test
  fun approve_pojos() {
    // tag::approve_pojos[]
    approve(exampleObject.createSomePerson()) // <1>
      .verify() // <2>
    // end::approve_pojos[]
  }

  @Test
  fun object_printer() {
    // tag::object_printer[]
    approve(exampleObject.createSomePerson()) // <1>
      .printWith(objectPrinter()) // <2>
      .verify()
    // end::object_printer[]
  }

  @Test
  fun custom_printer() {
    // tag::custom_printer[]
    approve(exampleObject.createSomePerson()) // <1>
      .printWith({ person -> "%s, born %s".format(person.name, person.birthDate) }) // <2>
      .verify()
    // end::custom_printer[]
  }

  @Test
  fun scrubbing() {
    // tag::scrubbing[]
    approve(exampleObject.createSomeBlogPost("Latest News", "Lorem ipsum dolor sit amet, consectetur adipiscing elit."))
      .printWith(objectPrinter())
      .scrubbedOf(instants(ISO_LOCAL_DATE_TIME)) // <1>
      .scrubbedOf(uuids()) // <2>
      .verify()
    // end::scrubbing[]
  }

  @Test
  fun custom_scrubbing() {
    // tag::custom_scrubbing[]
    approve(exampleObject.createContact("Jane Doe", "jane@approvej.org", "+1 123 456 7890"))
      .scrubbedOf { Contact(-1, it.name, it.email, it.phoneNumber) } // <1>
      .printWith(objectPrinter())
      .verify() // <2>
    // end::custom_scrubbing[]
  }

  @Test
  fun verifyInplace() {
    // tag::verify_inplace[]
    approve(exampleObject.createSomePerson())
      .verify(inplace("Person[name=John Doe, birthDate=1990-01-01]"))
    // end::verify_inplace[]
  }

  @Test
  fun verify_file_base_path() {
    // tag::verify_file_base_path[]
    approve(exampleObject.createSomePerson())
      .printWith(personYamlPrinter())
      .verify(
        file(
          approvedPath(
            Path.of(
              "src/test/resources/BasicExamples-verify_file_base_path.yaml" // <1>
            )
          )
        )
      )
    // end::verify_file_base_path[]
  }
}
