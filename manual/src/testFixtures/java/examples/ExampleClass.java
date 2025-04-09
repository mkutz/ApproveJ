package examples;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;

public class ExampleClass {

  public String createSomeSimpleString() {
    return "This is a simple string";
  }

  public Person createSomePerson() {
    return new Person("John Doe", LocalDate.of(1990, 1, 1));
  }

  public record Person(String name, LocalDate birthDate) {}

  public static Function<Person, String> personYamlPrinter() {
    return person ->
        """
        person:
          name: %s
          birthDate: %s
        """
            .formatted(person.name(), person.birthDate());
  }

  public BlogPost createSomeBlogPost(String title, String content) {
    return new BlogPost(title, content);
  }

  public record BlogPost(String title, String content, LocalDateTime published, UUID id) {
    public BlogPost(String title, String content) {
      this(title, content, LocalDateTime.now(), UUID.randomUUID());
    }
  }

  public Contact createContact(String name, String email, String phoneNumber) {
    return new Contact(name, email, phoneNumber);
  }

  public record Contact(int number, String name, String email, String phoneNumber) {
    static int counter = 0;

    public Contact(String name, String email, String phoneNumber) {
      this(++counter, name, email, phoneNumber);
    }
  }
}
