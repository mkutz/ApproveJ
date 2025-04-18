package examples.kotlin

import com.fasterxml.jackson.core.JsonProcessingException
import org.junit.jupiter.api.Test
import java.time.LocalDate
import examples.ExampleClass.createPerson
import org.approvej.ApprovalBuilder.approve
import org.approvej.yaml.jackson.YamlPrinter.yamlPrinter

class YamlJacksonDocTest {

    @Test
    @Throws(JsonProcessingException::class)
    fun printYaml() {
        // tag::print_yaml[]
        val person = createPerson("John Doe", LocalDate.of(1990, 1, 1))

        approve(person)
            .printWith(yamlPrinter()) // <1>
            .verify()
        // end::print_yaml[]
    }
}
