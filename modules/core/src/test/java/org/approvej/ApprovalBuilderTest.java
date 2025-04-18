package org.approvej;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.scrub.Scrubbers.dates;
import static org.approvej.scrub.Scrubbers.relativeDates;
import static org.approvej.scrub.Scrubbers.uuids;
import static org.approvej.verify.Verifiers.inFile;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import java.time.LocalDate;
import java.util.UUID;
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
  void verify_string_inplace() {
    approve(EXAMPLE_TEXT).verify(EXAMPLE_TEXT);
  }

  @Test
  void verify_string_in_file() {
    approve(EXAMPLE_TEXT)
        .scrubbedOf(relativeDates(ofPattern("yyyy-MM-dd")))
        .scrubbedOf(uuids())
        .verify(inFile());
  }

  @Test
  void verify_with_scrubbers() {
    approve(EXAMPLE_TEXT)
        .scrubbedOf(relativeDates(ofPattern("yyyy-MM-dd")))
        .scrubbedOf(uuids())
        .verify(SCRUBBED);
  }

  @Test
  void verify_failure() {
    assertThatExceptionOfType(AssertionError.class)
        .isThrownBy(() -> approve(EXAMPLE_TEXT).verify("This is not the same text."))
        .withMessage(
            "Approval mismatch: expected: <This is not the same text.> but was: <%s>"
                .formatted(EXAMPLE_TEXT));
  }

  @Test
  void verify_pre_and_post_print_scrubbing() {
    approve(new Person("Micha", LocalDate.of(1982, 2, 19)))
        .scrubbedOf(person -> new Person("[scrubbed id]", person.name, person.birthday))
        .printWith(Object::toString)
        .scrubbedOf(dates(ofPattern("yyyy-MM-dd")))
        .verify();
  }

  @Test
  void verify_default_printer() {
    approve(new Person("000000-0000-0000-00000001", "Micha", LocalDate.of(1982, 2, 19)))
        .verify("Person[id=000000-0000-0000-00000001, name=Micha, birthday=1982-02-19]");
  }

  record Person(String id, String name, LocalDate birthday) {
    Person(String name, LocalDate birthday) {
      this(UUID.randomUUID().toString(), name, birthday);
    }
  }
}
