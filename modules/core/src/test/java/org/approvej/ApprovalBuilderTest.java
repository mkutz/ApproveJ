package org.approvej;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.approve.Approvers.file;
import static org.approvej.approve.PathProviderBuilder.nextToTest;
import static org.approvej.print.ObjectPrinter.objectPrinter;
import static org.approvej.scrub.Scrubbers.dateTimeFormat;
import static org.approvej.scrub.Scrubbers.relativeDates;
import static org.approvej.scrub.Scrubbers.uuids;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class ApprovalBuilderTest {

  private static final String EXAMPLE_TEXT =
      """
      This is my example text with a date: %s.
      There is also a UUID here: %s.
      Also here's a timestamp: %s, which will be partially scrubbed.
      """
          .formatted(LocalDate.now(), UUID.randomUUID(), LocalDate.now().atTime(11, 22, 33));

  private static final String SCRUBBED =
      """
      This is my example text with a date: [today].
      There is also a UUID here: [uuid 1].
      Also here's a timestamp: [today]T11:22:33, which will be partially scrubbed.
      """;

  @Test
  void approve_string_inplace() {
    approve(EXAMPLE_TEXT).byValue(EXAMPLE_TEXT);
  }

  @Test
  void approve_string_by_approver() {
    approve(EXAMPLE_TEXT)
        .scrubbedOf(relativeDates(ofPattern("yyyy-MM-dd")))
        .scrubbedOf(uuids())
        .by(file(nextToTest().filenameExtension("txt")));
  }

  @Test
  void approve_string_byFile() {
    approve(EXAMPLE_TEXT)
        .scrubbedOf(relativeDates(ofPattern("yyyy-MM-dd")))
        .scrubbedOf(uuids())
        .byFile();
  }

  @Test
  void approve_string_byFile_custom_pathProvider() {
    approve(EXAMPLE_TEXT)
        .scrubbedOf(relativeDates(ofPattern("yyyy-MM-dd")))
        .scrubbedOf(uuids())
        .byFile(nextToTest());
  }

  @Test
  void approve_string_byFile_custom_pathProviderBuilder() {
    approve(EXAMPLE_TEXT)
        .scrubbedOf(relativeDates(ofPattern("yyyy-MM-dd")))
        .scrubbedOf(uuids())
        .byFile(nextToTest());
  }

  @Test
  void approve_string_byFile_custom_pathProviderBuilder_path() {
    approve(EXAMPLE_TEXT)
        .scrubbedOf(relativeDates(ofPattern("yyyy-MM-dd")))
        .scrubbedOf(uuids())
        .byFile(
            Path.of(
                "src/test/resources/approve_string_byFile_custom_pathProviderBuilder-approved.txt"));
  }

  @Test
  void approve_string_byFile_custom_pathProviderBuilder_path_string() {
    approve(EXAMPLE_TEXT)
        .scrubbedOf(relativeDates(ofPattern("yyyy-MM-dd")))
        .scrubbedOf(uuids())
        .byFile(
            "src/test/resources/approve_string_byFile_custom_pathProviderBuilder_path_string-approved.txt");
  }

  @Test
  void approve_string_byValue() {
    approve(EXAMPLE_TEXT)
        .scrubbedOf(relativeDates(ofPattern("yyyy-MM-dd")))
        .scrubbedOf(uuids())
        .byValue(SCRUBBED);
  }

  @Test
  void approve_string_mismatch() {
    assertThatExceptionOfType(AssertionError.class)
        .isThrownBy(() -> approve(EXAMPLE_TEXT).byValue("This is not the same text."))
        .withMessage(
            "Approval mismatch: expected: <This is not the same text.> but was: <%s>"
                .formatted(EXAMPLE_TEXT));
  }

  @Test
  void approve_pojo_default_printer() {
    approve(new Person("000000-0000-0000-00000001", "Micha", LocalDate.of(1982, 2, 19)))
        .byValue("Person[id=000000-0000-0000-00000001, name=Micha, birthday=1982-02-19]");
  }

  @Test
  void approve_pojo_printWith_function() {
    Function<Person, String> personPrinter =
        person -> "id=%s%nname=%s%nbirthday=%s".formatted(person.id, person.name, person.birthday);
    approve(new Person("000000-0000-0000-00000001", "Micha", LocalDate.of(1982, 2, 19)))
        .printWith(personPrinter)
        .byValue("id=000000-0000-0000-00000001\nname=Micha\nbirthday=1982-02-19");
  }

  @Test
  void approve_pojo_printWith_printer() {
    approve(new Person("000000-0000-0000-00000001", "Micha", LocalDate.of(1982, 2, 19)))
        .printWith(objectPrinter())
        .byValue(
            """
            Person [
              birthday=<inaccessible>,
              id=<inaccessible>,
              name=<inaccessible>
            ]""");
  }

  @Test
  void approve_pojo_byFile_pre_and_post_print_scrubbing() {
    approve(new Person("Micha", LocalDate.of(1982, 2, 19)))
        .scrubbedOf(person -> new Person("[scrubbed id]", person.name, person.birthday))
        .printWith(Object::toString)
        .scrubbedOf(dateTimeFormat("yyyy-MM-dd"))
        .byFile();
  }

  record Person(String id, String name, LocalDate birthday) {
    Person(String name, LocalDate birthday) {
      this(UUID.randomUUID().toString(), name, birthday);
    }
  }
}
