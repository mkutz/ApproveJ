package examples.kotlin

import examples.ExampleClass.Person
import examples.ExampleClass.createPerson
import java.time.LocalDate
import org.approvej.ApprovalBuilder.approve
import org.approvej.print.PrintFormat
import org.approvej.print.Printer
import org.approvej.review.Reviewers
import org.assertj.core.api.Assumptions.assumeThat
import org.junit.jupiter.api.Test

@org.approvej.ApprovalTest
class ReviewingDocTest {

  @Test
  fun `approve reviewedBy fileReviewer`() {
    assumeThat(ProcessBuilder("which", "meld").start().waitFor()).isEqualTo(0)
    // tag::approve_reviewedBy_fileReviewer[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printedAs(PersonYamlPrinter())
      .reviewedBy("meld \"{receivedFile}\" \"{approvedFile}\"") // <1>
      .byFile() // <2>
    // end::approve_reviewedBy_fileReviewer[]
  }

  @Test
  fun `approve reviewedBy automatic`() {
    // tag::approve_reviewedBy_automatic[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person).printedAs(PersonYamlPrinter()).reviewedBy(Reviewers.automatic()).byFile()
    // end::approve_reviewedBy_automatic[]
  }

  class PersonYamlPrinter : PrintFormat<Person> {
    override fun printer() =
      Printer<Person> { person ->
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
