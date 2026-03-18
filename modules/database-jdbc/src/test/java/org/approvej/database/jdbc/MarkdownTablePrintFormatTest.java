package org.approvej.database.jdbc;

import static org.approvej.database.jdbc.MarkdownTablePrintFormat.markdownTable;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class MarkdownTablePrintFormatTest {

  @Test
  void printer() {
    QueryResult result =
        new QueryResult(
            List.of("id", "name", "email"),
            List.of(List.of("1", "Alice", "alice@test.com"), List.of("2", "Bob", "bob@test.com")));

    String printed = markdownTable().printer().apply(result);

    assertThat(printed)
        .isEqualTo(
            """
            | id | name  | email          |
            |----|-------|----------------|
            | 1  | Alice | alice@test.com |
            | 2  | Bob   | bob@test.com   |\
            """);
  }

  @Test
  void printer_empty_rows() {
    QueryResult result = new QueryResult(List.of("id", "name"), List.of());

    String printed = markdownTable().printer().apply(result);

    assertThat(printed)
        .isEqualTo(
            """
            | id | name |
            |----|------|\
            """);
  }

  @Test
  void printer_single_column() {
    QueryResult result = new QueryResult(List.of("name"), List.of(List.of("Alice")));

    String printed = markdownTable().printer().apply(result);

    assertThat(printed)
        .isEqualTo(
            """
            | name  |
            |-------|
            | Alice |\
            """);
  }

  @Test
  void printer_wide_values() {
    QueryResult result =
        new QueryResult(
            List.of("id", "description"), List.of(List.of("1", "a very long description value")));

    String printed = markdownTable().printer().apply(result);

    assertThat(printed)
        .isEqualTo(
            """
            | id | description                   |
            |----|-------------------------------|
            | 1  | a very long description value |\
            """);
  }

  @Test
  void filenameExtension() {
    assertThat(markdownTable().filenameExtension()).isEqualTo("md");
  }
}
