package org.approvej;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.scrub.RelativeDateScrubber.relativeDates;
import static org.approvej.scrub.UuidScrubber.uuids;
import static org.approvej.verify.FileVerifier.file;
import static org.approvej.verify.InplaceVerifier.inplace;
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
  void verify() {
    approve(EXAMPLE_TEXT).verify(inplace(EXAMPLE_TEXT));
  }

  @Test
  void verify_file() {
    approve(EXAMPLE_TEXT)
        .scrubbedOf(relativeDates(ofPattern("yyyy-MM-dd")))
        .scrubbedOf(uuids())
        .verify(file());
  }

  @Test
  void verify_with_scrubbers() {
    approve(EXAMPLE_TEXT)
        .scrubbedOf(relativeDates(ofPattern("yyyy-MM-dd")))
        .scrubbedOf(uuids())
        .verify(inplace(SCRUBBED));
  }

  @Test
  void verify_failure() {
    assertThatExceptionOfType(AssertionError.class)
        .isThrownBy(() -> approve(EXAMPLE_TEXT).verify(inplace("This is not the same text.")))
        .withMessage(
            "Approval mismatch: expected: <%s> but was: <This is not the same text.>"
                .formatted(EXAMPLE_TEXT));
  }
}
