package org.approvej.database;

import static org.approvej.database.QueryResultPrintFormat.queryResult;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class QueryResultPrintFormatTest {

  @Test
  void printer() {
    QueryResult result =
        new QueryResult(
            "SELECT * FROM users",
            List.of("id", "name", "email"),
            List.of(List.of("1", "Alice", "alice@test.com"), List.of("2", "Bob", "bob@test.com")));

    String printed = queryResult().printer().apply(result);

    assertThat(printed)
        .isEqualTo(
            """
            query: SELECT * FROM users

            | id | name  | email          |
            |----|-------|----------------|
            | 1  | Alice | alice@test.com |
            | 2  | Bob   | bob@test.com   |\
            """);
  }

  @Test
  void printer_empty_rows() {
    QueryResult result =
        new QueryResult("SELECT * FROM users WHERE 1=0", List.of("id", "name"), List.of());

    String printed = queryResult().printer().apply(result);

    assertThat(printed)
        .isEqualTo(
            """
            query: SELECT * FROM users WHERE 1=0

            | id | name |
            |----|------|\
            """);
  }

  @Test
  void printer_single_column() {
    QueryResult result =
        new QueryResult("SELECT name FROM users", List.of("name"), List.of(List.of("Alice")));

    String printed = queryResult().printer().apply(result);

    assertThat(printed)
        .isEqualTo(
            """
            query: SELECT name FROM users

            | name  |
            |-------|
            | Alice |\
            """);
  }

  @Test
  void printer_wide_values() {
    QueryResult result =
        new QueryResult(
            "SELECT * FROM t",
            List.of("id", "description"),
            List.of(List.of("1", "a very long description value")));

    String printed = queryResult().printer().apply(result);

    assertThat(printed)
        .isEqualTo(
            """
            query: SELECT * FROM t

            | id | description                   |
            |----|-------------------------------|
            | 1  | a very long description value |\
            """);
  }

  @Test
  void filenameExtension() {
    assertThat(queryResult().filenameExtension()).isEqualTo("md");
  }
}
