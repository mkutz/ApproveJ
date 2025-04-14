package examples;

import static java.util.stream.Collectors.joining;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

  public String createSomeTaggedBlogPost(String title, String content, List<Tag> tagIds) {
    var published = Instant.now();
    var id = UUID.randomUUID();
    return """
           {
             "id": "%s",
             "title": "%s",
             "content": "%s",
             "tagIds": [%s],
             "published": "%s"
           }
           """
        .formatted(
            id,
            title,
            content,
            tagIds.stream()
                .map(tag -> "\"%s\"".formatted(tag.id().toString()))
                .collect(joining(",")),
            published);
  }

  public enum Tag {
    NEWS(UUID.fromString("00000000-0000-0000-0000-000000000001")),
    ENTERTAINMENT(UUID.fromString("00000000-0000-0000-0000-000000000002")),
    SPORTS(UUID.fromString("00000000-0000-0000-0000-000000000003")),
    TECHNOLOGY(UUID.fromString("00000000-0000-0000-0000-000000000004")),
    ;

    private final UUID id;

    Tag(UUID id) {
      this.id = id;
    }

    public UUID id() {
      return id;
    }
  }
}
