package examples.kotlin

import examples.ExampleClass.createPerson
import examples.java.ScreamingPrintFormat
import java.time.LocalDate
import org.approvej.ApprovalBuilder.approve
import org.junit.jupiter.api.Test

@org.approvej.ApprovalTest
class ConfigurationDocTest {

  @Test
  fun `screaming print format`() {
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printedAs(ScreamingPrintFormat())
      .byValue("PERSON[NAME=JOHN DOE, BIRTHDATE=1990-01-01]")
  }
}
