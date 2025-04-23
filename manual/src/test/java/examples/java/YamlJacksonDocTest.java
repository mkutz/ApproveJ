package examples.java;

import static examples.ExampleClass.createPerson;
import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.yaml.jackson.YamlPrinter.yamlPrinter;

import examples.ExampleClass.Person;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class YamlJacksonDocTest {

  @Test
  void print_yaml() {
    // tag::print_yaml[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printWith(yamlPrinter()) // <1>
        .byFile();
    // end::print_yaml[]
  }
}
