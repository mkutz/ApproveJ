package org.approvej.database;

import static org.approvej.scrub.Replacements.numbered;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ColumnValueScrubberTest {

  @Test
  void apply() {
    QueryResult result =
        new QueryResult(
            "SELECT * FROM users",
            List.of("id", "name"),
            List.of(List.of("1", "Alice"), List.of("2", "Bob")));

    QueryResult scrubbed = DatabaseScrubbers.columnValue("id").apply(result);

    assertThat(scrubbed.rows().get(0)).containsExactly("[id]", "Alice");
    assertThat(scrubbed.rows().get(1)).containsExactly("[id]", "Bob");
  }

  @Test
  void apply_nonexistent_column() {
    QueryResult result =
        new QueryResult(
            "SELECT * FROM users", List.of("id", "name"), List.of(List.of("1", "Alice")));

    QueryResult scrubbed = DatabaseScrubbers.columnValue("nonexistent").apply(result);

    assertThat(scrubbed.rows().get(0)).containsExactly("1", "Alice");
  }

  @Test
  void replacement() {
    QueryResult result =
        new QueryResult(
            "SELECT * FROM users", List.of("id", "name"), List.of(List.of("1", "Alice")));

    QueryResult scrubbed =
        DatabaseScrubbers.columnValue("id").replacement(numbered("id")).apply(result);

    assertThat(scrubbed.rows().get(0)).containsExactly("[id 1]", "Alice");
  }

  @Test
  void replacement_static() {
    QueryResult result =
        new QueryResult(
            "SELECT * FROM users", List.of("id", "name"), List.of(List.of("1", "Alice")));

    QueryResult scrubbed = DatabaseScrubbers.columnValue("id").replacement("***").apply(result);

    assertThat(scrubbed.rows().get(0)).containsExactly("***", "Alice");
  }
}
