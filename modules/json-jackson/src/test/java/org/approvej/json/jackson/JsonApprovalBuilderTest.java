package org.approvej.json.jackson;

import static org.approvej.ApprovalBuilder.approve;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.approvej.scrub.RelativeDateScrubber;
import org.approvej.scrub.UuidScrubber;
import org.approvej.verify.FileVerifier;
import org.approvej.verify.InplaceVerifier;
import org.junit.jupiter.api.Test;

class JsonApprovalBuilderTest {

  private static final String EXAMPLE_JSON =
      """
      {
        "date": "%s",
        "id": "%s",
        "timestamp": "%s"
      }"""
          .trim()
          .stripIndent()
          .formatted(LocalDate.now(), UUID.randomUUID(), LocalDate.now().atTime(11, 22, 33));

  private static final String EXAMPLE_MINIMAL_JSON =
      "{\"date\":\"%s\",\"id\":\"%s\",\"timestamp\": \"%s\"}"
          .stripIndent()
          .formatted(LocalDate.now(), UUID.randomUUID(), LocalDate.now().atTime(11, 22, 33));

  private static final String SCRUBBED_JSON =
      """
      {
        "date": "[today]",
        "id": "[uuid 1]",
        "timestamp": "[today]T11:22:33"
      }
      """
          .trim()
          .stripIndent();

  @Test
  void verify_list() {
    approve(List.of("a", "b", "c"))
        .printWith(new JsonPrettyPrinter<>())
        .verify(new InplaceVerifier("[ \"a\", \"b\", \"c\" ]"));
  }

  @Test
  void verify_file() {
    RelativeDateScrubber dates =
        new RelativeDateScrubber(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    UuidScrubber uuids = new UuidScrubber();
    approve(EXAMPLE_JSON).scrubbedWith(dates).scrubbedWith(uuids).verify(new FileVerifier("json"));
  }

  @Test
  void verify_with_scrubbers() {
    RelativeDateScrubber dates =
        new RelativeDateScrubber(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    UuidScrubber uuids = new UuidScrubber();
    approve(EXAMPLE_JSON)
        .scrubbedWith(dates)
        .scrubbedWith(uuids)
        .verify(new InplaceVerifier(SCRUBBED_JSON));
  }

  @Test
  void verify_failure() {
    assertThatExceptionOfType(AssertionError.class)
        .isThrownBy(
            () -> approve(EXAMPLE_JSON).verify(new InplaceVerifier("This is not the same text.")))
        .withMessage(
            "Approval mismatch: expected: <%s> but was: <This is not the same text.>"
                .formatted(EXAMPLE_JSON));
  }
}
