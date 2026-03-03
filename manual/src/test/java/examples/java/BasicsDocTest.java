package examples.java;

import static examples.ExampleClass.createPerson;
import static examples.ExampleClass.hello;
import static org.approvej.ApprovalBuilder.approve;

import examples.ExampleClass.Person;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

@org.approvej.ApprovalTest
class BasicsDocTest {

  @Test
  void approve_strings() {
    // tag::approve_strings[]
    String result = hello("World");

    approve(result) // <1>
        .byFile(); // <2>
    // end::approve_strings[]
  }

  @Test
  void approve_pojos() {
    // tag::approve_pojos[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person) // <1>
        .byFile();
    // end::approve_pojos[]
  }

  @Test
  void approve_named() {
    // tag::approve_named[]
    Person jane = createPerson("Jane Doe", LocalDate.of(1990, 1, 1));
    Person john = createPerson("John Doe", LocalDate.of(2012, 6, 2));

    approve(jane).named("jane").byFile();
    approve(john).named("john").byFile();
    // end::approve_named[]
  }
}
