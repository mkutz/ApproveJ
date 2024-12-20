package org.approvej;

import static org.approvej.Approvals.approve;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
  void verify_with_scrubbers() {
    var dates = new RelativeDateScrubber(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    var uuids = new UuidScrubber();
    approve(EXAMPLE_TEXT).scrubbedWith(dates).scrubbedWith(uuids).verify(SCRUBBED);
  }

  @Test
  void verify_without_scrubbers() {
    approve(EXAMPLE_TEXT).verify(EXAMPLE_TEXT);
  }
}
