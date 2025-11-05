package org.approvej.json.jackson;

import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.json.jackson.JsonPrettyPrinter.jsonPrettyPrinter;
import static org.approvej.json.jackson.JsonStringPrettyPrinter.jsonStringPrettyPrinter;
import static org.approvej.scrub.Scrubbers.dateTimeFormat;
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
      }\
      """
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
  void approve_list() {
    approve(List.of("a", "b", "c"))
        .printedBy(jsonPrettyPrinter())
        .byValue("[ \"a\", \"b\", \"c\" ]");
  }

  @Test
  void approve_file() {
    approve(EXAMPLE_JSON)
        .printedBy(jsonStringPrettyPrinter())
        .scrubbedOf(dateTimeFormat("yyyy-MM-dd").replaceWithRelativeDate())
        .scrubbedOf(uuids())
        .byFile();
  }

  @Test
  void approve_with_scrubbers() {
    approve(EXAMPLE_JSON)
        .printedBy(jsonStringPrettyPrinter())
        .scrubbedOf(dateTimeFormat("yyyy-MM-dd").replaceWithRelativeDate())
        .scrubbedOf(uuids())
        .byValue(SCRUBBED_JSON);
  }

  @Test
  void approve_failure() {
    assertThatExceptionOfType(AssertionError.class)
        .isThrownBy(() -> approve(EXAMPLE_JSON).byValue("This is not the same text."))
        .withMessage(
            """
            Approval mismatch:
            expected:
              "This is not the same text."
             but was:
              "%s"
            """
                .formatted(EXAMPLE_JSON.indent(2).trim()));
  }
}
