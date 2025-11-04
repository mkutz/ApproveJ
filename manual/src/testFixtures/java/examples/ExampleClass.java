package examples;

import static java.util.stream.Collectors.joining;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ExampleClass {

  public static String hello(String greeting) {
    return "Hello, %s!".formatted(greeting);
  }

  public static Person createPerson(String name, LocalDate birthDate) {
    return new Person(name, birthDate);
  }

  // tag::person_pojo[]
  public record Person(String name, LocalDate birthDate) {}

  // end::person_pojo[]

  public static BlogPost createBlogPost(String title, String content) {
    return new BlogPost(title, content);
  }

  public static
  // tag::blog_post_pojo[]
  class BlogPost {
    private final UUID id;
    private final String title;
    private final String content;
    private final Instant published;

    public BlogPost(String title, String content) {
      this.id = UUID.randomUUID(); // <1>
      this.title = title;
      this.content = content;
      this.published = Instant.now(); // <2>
    }

    public String title() {
      return title;
    }

    public String content() {
      return content;
    }

    public Instant published() {
      return published;
    }

    public UUID id() {
      return id;
    }

    @Override
    public String toString() {
      return "BlogPost[title=%s, content=%s, published=%s, id=%s]"
          .formatted(title, content, published, id);
    }
  }

  // end::blog_post_pojo[]

  public static Contact createContact(String name, String email, String phoneNumber) {
    return new Contact(name, email, phoneNumber);
  }

  public record Contact(int number, String name, String email, String phoneNumber) {
    static int counter = 0;

    public Contact(String name, String email, String phoneNumber) {
      this(++counter, name, email, phoneNumber);
    }
  }

  public static String createTaggedBlogPost(String title, String content, List<Tag> tagIds) {
    Instant published = Instant.now();
    UUID id = UUID.randomUUID();
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
    TECHNOLOGY(UUID.fromString("00000000-0000-0000-0000-000000000004"));

    private final UUID id;

    Tag(UUID id) {
      this.id = id;
    }

    public UUID id() {
      return id;
    }
  }
}
