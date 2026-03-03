package examples.java;

import static examples.ExampleClass.createPerson;
import static org.approvej.ApprovalBuilder.approve;

import examples.ExampleClass.Person;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

@org.approvej.ApprovalTest
class ConfigurationDocTest {

  @Test
  void screaming_print_format() {
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printedAs(new ScreamingPrintFormat())
        .byValue("PERSON[NAME=JOHN DOE, BIRTHDATE=1990-01-01]");
  }
}
