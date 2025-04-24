package org.approvej.json.jackson;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.json.jackson.JsonPrettyPrinter.jsonPrettyPrinter;
import static org.approvej.json.jackson.JsonStringPrettyPrinter.jsonStringPrettyPrinter;
import static org.approvej.scrub.Scrubbers.relativeDates;
import static org.approvej.scrub.Scrubbers.uuids;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
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

  private static final String SCRUBBED_JSON =
      """
      {
        "date" : "[today]",
        "id" : "[uuid 1]",
        "timestamp" : "[today]T11:22:33"
      }
      """
          .trim()
          .stripIndent();

  @Test
  void verify_list() {
    approve(List.of("a", "b", "c"))
        .printWith(jsonPrettyPrinter())
        .byValue("[ \"a\", \"b\", \"c\" ]");
  }

  @Test
  void verify_file() {
    approve(EXAMPLE_JSON)
        .printWith(jsonStringPrettyPrinter())
        .scrubbedOf(relativeDates(ofPattern("yyyy-MM-dd")))
        .scrubbedOf(uuids())
        .byFile();
  }

  @Test
  void verify_with_scrubbers() {
    approve(EXAMPLE_JSON)
        .printWith(jsonStringPrettyPrinter())
        .scrubbedOf(relativeDates(ofPattern("yyyy-MM-dd")))
        .scrubbedOf(uuids())
        .byValue(SCRUBBED_JSON);
  }

  @Test
  void verify_failure() {
    assertThatExceptionOfType(AssertionError.class)
        .isThrownBy(() -> approve(EXAMPLE_JSON).byValue("This is not the same text."))
        .withMessage(
            "Approval mismatch: previously approved: <This is not the same text.>, received: <%s>"
                .formatted(EXAMPLE_JSON));
  }
}
