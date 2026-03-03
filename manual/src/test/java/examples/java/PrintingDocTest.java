package examples.java;

import static examples.ExampleClass.createPerson;
import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.print.MultiLineStringPrintFormat.multiLineString;

import examples.ExampleClass.Person;
import examples.PersonYamlPrintFormat;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

@org.approvej.ApprovalTest
class PrintingDocTest {

  @Test
  void multi_line_string_format() {
    // tag::multi_line_string_format[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printedAs(multiLineString()) // <1>
        .byFile();
    // end::multi_line_string_format[]
  }

  @Test
  void custom_printer_function() {
    // tag::custom_printer_function[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printedBy(it -> String.format("%s, born %s", it.name(), it.birthDate())) // <1>
        .byFile();
    // end::custom_printer_function[]
  }

  @Test
  void custom_print_format() {
    // tag::custom_print_format[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printedAs(new PersonYamlPrintFormat()) // <1>
        .byFile();
    // end::custom_print_format[]
  }

  @Test
  void custom_print_format_provider() {
    // tag::custom_print_format_provider[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printedAs(new ScreamingPrintFormat()) // <1>
        .byFile();
    // end::custom_print_format_provider[]
  }
}
