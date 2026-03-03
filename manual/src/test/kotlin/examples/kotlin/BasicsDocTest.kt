package examples.kotlin

import examples.ExampleClass.createPerson
import examples.ExampleClass.hello
import java.time.LocalDate
import org.approvej.ApprovalBuilder.approve
import org.junit.jupiter.api.Test

@org.approvej.ApprovalTest
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
  fun `approve named`() {
    // tag::approve_named[]
    val jane = createPerson("Jane Doe", LocalDate.of(1990, 1, 1))
    val john = createPerson("John Doe", LocalDate.of(2012, 6, 2))

    approve(jane).named("jane").byFile()
    approve(john).named("john").byFile()
    // end::approve_named[]
  }
}
