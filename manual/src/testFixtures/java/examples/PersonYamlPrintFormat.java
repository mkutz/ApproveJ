package examples;

import examples.ExampleClass.Person;
import org.approvej.print.PrintFormat;
import org.approvej.print.Printer;

// tag::person_yaml_print_format[]
public class PersonYamlPrintFormat implements PrintFormat<Person> {
  @Override
  public Printer<Person> printer() {
    return (Person person) ->
        """
        person:
          name: "%s"
          birthDate: "%s"
        """
            .formatted(person.name(), person.birthDate());
  }

  @Override
  public String filenameExtension() {
    return "yaml";
  }
}
// end::person_yaml_print_format[]
