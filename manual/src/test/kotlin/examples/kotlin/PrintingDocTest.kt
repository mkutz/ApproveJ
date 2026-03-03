package examples.kotlin

import examples.ExampleClass.Person
import examples.ExampleClass.createPerson
import examples.java.ScreamingPrintFormat
import java.time.LocalDate
import org.approvej.ApprovalBuilder.approve
import org.approvej.print.MultiLineStringPrintFormat.multiLineString
import org.approvej.print.PrintFormat
import org.approvej.print.Printer
import org.junit.jupiter.api.Test

@org.approvej.ApprovalTest
class PrintingDocTest {

  @Test
  fun `multi line string format`() {
    // tag::multi_line_string_format[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printedAs(multiLineString()) // <1>
      .byFile()
    // end::multi_line_string_format[]
  }

  @Test
  fun `custom printer function`() {
    // tag::custom_printer_function[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printedBy { "%s, born %s".format(it.name, it.birthDate) } // <1>
      .byFile()
    // end::custom_printer_function[]
  }

  @Test
  fun `custom print format`() {
    // tag::custom_print_format[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printedAs(PersonYamlPrinter()) // <1>
      .byFile()
    // end::custom_print_format[]
  }

  @Test
  fun custom_print_format_provider() {
    // tag::custom_print_format_provider[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printedAs(ScreamingPrintFormat()) // <1>
      .byFile()
    // end::custom_print_format_provider[]
  }

  // tag::person_yaml_print_format[]
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
  // end::person_yaml_print_format[]
}
