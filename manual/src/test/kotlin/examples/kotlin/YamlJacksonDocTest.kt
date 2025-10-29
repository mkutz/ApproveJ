package examples.kotlin

import examples.ExampleClass.createPerson
import java.time.LocalDate
import org.approvej.ApprovalBuilder.approve
import org.approvej.yaml.jackson.YamlPrinter.yamlPrinter
import org.junit.jupiter.api.Test

class YamlJacksonDocTest {

  @Test
  fun printYaml() {
    // tag::print_yaml[]
    val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

    approve(person)
      .printedWith(yamlPrinter()) // <1>
      .byFile()
    // end::print_yaml[]
  }
}
