package org.approvej;

import static org.approvej.ApprovalBuilder.approve;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.approvej.scrub.RelativeDateScrubber;
import org.approvej.scrub.UuidScrubber;
import org.approvej.verify.FileVerifier;
import org.approvej.verify.InplaceVerifier;
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
    approve(EXAMPLE_TEXT).verify(new InplaceVerifier(EXAMPLE_TEXT));
  }

  @Test
  void verify_file() {
    RelativeDateScrubber dates =
        new RelativeDateScrubber(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    UuidScrubber uuids = new UuidScrubber();
    approve(EXAMPLE_TEXT).scrubbedWith(dates).scrubbedWith(uuids).verify(new FileVerifier());
  }

  @Test
  void verify_with_scrubbers() {
    RelativeDateScrubber dates =
        new RelativeDateScrubber(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    UuidScrubber uuids = new UuidScrubber();
    approve(EXAMPLE_TEXT)
        .scrubbedWith(dates)
        .scrubbedWith(uuids)
        .verify(new InplaceVerifier(SCRUBBED));
  }

  @Test
  void verify_failure() {
    assertThatExceptionOfType(AssertionError.class)
        .isThrownBy(
            () -> approve(EXAMPLE_TEXT).verify(new InplaceVerifier("This is not the same text.")))
        .withMessage(
            "Approval mismatch: expected: <%s> but was: <This is not the same text.>"
                .formatted(EXAMPLE_TEXT));
  }
}
